package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CreateCommunityRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityCategory;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.JoinType;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommunityServiceTest {
    private UserRepository userRepository;
    private CommunityRepository communityRepository;
    private UserCommunityRepository userCommunityRepository;
    private TransactionalOperator transactionalOperator;
    private UserService userService;
    private ModelMapper modelMapper;

    private CommunityService communityService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        communityRepository = mock(CommunityRepository.class);
        userCommunityRepository = mock(UserCommunityRepository.class);
        transactionalOperator = mock(TransactionalOperator.class);
        userService = mock(UserService.class);
        modelMapper = mock(ModelMapper.class);
        communityService = new CommunityService(userRepository, communityRepository, userCommunityRepository, userService, modelMapper, transactionalOperator);

        when(modelMapper.map(any(Community.class), eq(org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityDTO.class)))
                .thenAnswer(invocation -> {
                    Community source = invocation.getArgument(0);
                    var dto = new org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityDTO();
                    dto.setName(source.getName());
                    return dto;
                });

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getAll_shouldReturnAllCommunities() {
        Community community = new Community();
        community.setName("Test Community");

        when(communityRepository.findAll()).thenReturn(Flux.just(community));

        StepVerifier.create(communityService.getAll())
                .expectNextMatches(dto -> dto.getName().equals("Test Community"))
                .verifyComplete();
    }

    @Test
    void getById_whenCommunityExists_shouldReturnCommunityDTO() {
        UUID id = UUID.randomUUID();
        Community community = new Community();
        community.setId(id);
        community.setName("Community1");

        when(communityRepository.findById(id)).thenReturn(Mono.just(community));

        StepVerifier.create(communityService.getById(id))
                .expectNextMatches(dto -> dto.getName().equals("Community1"))
                .verifyComplete();
    }

    @Test
    void getById_whenCommunityNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(communityRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(communityService.getById(id))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Community not found"))
                .verify();
    }

    @Test
    void createCommunity_shouldSaveCommunityAndUserCommunity() {
        UUID adminId = UUID.randomUUID();
        CreateCommunityRequest request = new CreateCommunityRequest();
        request.setName("New Community");
        request.setCategory(CommunityCategory.GAMING);
        request.setJoinType(JoinType.OPEN);

        Community savedCommunity = new Community();
        savedCommunity.setId(UUID.randomUUID());
        savedCommunity.setName(request.getName());
        savedCommunity.setCategory(request.getCategory());
        savedCommunity.setJoinType(request.getJoinType());

        User adminUser = new User();
        adminUser.setId(adminId);

        when(communityRepository.save(any(Community.class))).thenReturn(Mono.just(savedCommunity));
        when(userRepository.findById(adminId)).thenReturn(Mono.just(adminUser));
        when(userCommunityRepository.save(any(UserCommunity.class))).thenReturn(Mono.just(new UserCommunity()));

        StepVerifier.create(communityService.createCommunity(Mono.just(request), adminId))
                .expectNextMatches(dto -> dto.getName().equals("New Community"))
                .verifyComplete();

        verify(communityRepository).save(any(Community.class));
        verify(userRepository).findById(adminId);
        verify(userCommunityRepository).save(any(UserCommunity.class));
    }

    @Test
    void deleteCommunityById_whenUserIsAdmin_shouldDeleteCommunity() {
        UUID communityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserCommunity adminUserCommunity = new UserCommunity(userId, communityId, CommunityRole.ADMIN);

        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId)).thenReturn(Mono.just(adminUserCommunity));
        when(userCommunityRepository.deleteByCommunityIdAndUserId(communityId, userId)).thenReturn(Mono.empty());
        when(communityRepository.deleteById(communityId)).thenReturn(Mono.empty());

        StepVerifier.create(communityService.deleteCommunityById(communityId, userId))
                .verifyComplete();

        verify(userCommunityRepository).deleteByCommunityIdAndUserId(communityId, userId);
        verify(communityRepository).deleteById(communityId);
    }

    @Test
    void deleteCommunityById_whenUserNotAdmin_shouldThrow() {
        UUID communityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserCommunity member = new UserCommunity(userId, communityId, CommunityRole.MEMBER);

        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId)).thenReturn(Mono.just(member));
        when(userCommunityRepository.deleteByCommunityIdAndUserId(any(), any())).thenReturn(Mono.empty());
        when(communityRepository.deleteById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(communityService.deleteCommunityById(communityId, userId))
                .expectError(NotAllowedException.class)
                .verify();
    }

    @Test
    void joinCommunity_whenJoinTypeOpen_shouldAddMember() {
        UUID communityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Community community = new Community();
        community.setId(communityId);
        community.setJoinType(JoinType.OPEN);

        UserCommunity userCommunity = new UserCommunity(userId, communityId, CommunityRole.MEMBER);

        when(communityRepository.findById(communityId)).thenReturn(Mono.just(community));
        when(userCommunityRepository.save(any(UserCommunity.class))).thenReturn(Mono.just(userCommunity));

        StepVerifier.create(communityService.joinCommunity(communityId, userId))
                .expectNextMatches(uc -> uc.getUserRole() == CommunityRole.MEMBER)
                .verifyComplete();
    }

    @Test
    void joinCommunity_whenJoinTypeNotOpen_shouldSendRequest() {
        UUID communityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Community community = new Community();
        community.setId(communityId);
        community.setJoinType(JoinType.CLOSED);

        UserCommunity waitingUserCommunity = new UserCommunity(userId, communityId, CommunityRole.WAITING_TO_ACCEPT);

        when(communityRepository.findById(communityId)).thenReturn(Mono.just(community));
        when(userCommunityRepository.save(any(UserCommunity.class))).thenReturn(Mono.just(waitingUserCommunity));

        StepVerifier.create(communityService.joinCommunity(communityId, userId))
                .expectNextMatches(uc -> uc.getUserRole() == CommunityRole.WAITING_TO_ACCEPT)
                .verifyComplete();
    }
}
