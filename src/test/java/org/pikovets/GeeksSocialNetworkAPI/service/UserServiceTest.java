package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.ChangeRoleRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.UserCommunityDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.repository.*;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserRepository userRepository;
    private UserCommunityRepository userCommunityRepository;
    private UserRelationshipRepository userRelationshipRepository;
    private CommunityRepository communityRepository;
    private ProfileRepository profileRepository;
    private ModelMapper modelMapper;
    private PasswordEncoder passwordEncoder;
    private TransactionalOperator transactionalOperator;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userCommunityRepository = mock(UserCommunityRepository.class);
        userRelationshipRepository = mock(UserRelationshipRepository.class);
        communityRepository = mock(CommunityRepository.class);
        profileRepository = mock(ProfileRepository.class);
        modelMapper = mock(ModelMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        transactionalOperator = mock(TransactionalOperator.class);

        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService = new UserService(
                userRepository,
                userRelationshipRepository,
                userCommunityRepository,
                communityRepository,
                profileRepository,
                modelMapper,
                passwordEncoder,
                transactionalOperator
        );
    }

    @Test
    void getAllUsers_returnsMappedUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        User user2 = new User();
        user2.setEmail("user2@example.com");
        UserDTO dto1 = new UserDTO();
        UserDTO dto2 = new UserDTO();

        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2));
        when(modelMapper.map(user1, UserDTO.class)).thenReturn(dto1);
        when(modelMapper.map(user2, UserDTO.class)).thenReturn(dto2);

        StepVerifier.create(userService.getAllUsers())
                .expectNext(dto1, dto2)
                .verifyComplete();
    }

    @Test
    void getUserById_found() {
        UUID id = UUID.randomUUID();
        User user = new User();
        UserDTO userDTO = new UserDTO();

        when(userRepository.findById(id)).thenReturn(Mono.just(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        StepVerifier.create(userService.getUserById(id))
                .expectNext(userDTO)
                .verifyComplete();
    }

    @Test
    void getUserById_notFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserById(id))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("User not found"))
                .verify();
    }

    @Test
    void getUserByEmail_found() {
        String email = "test@example.com";
        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.getUserByEmail(email))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void getUserByEmail_notFound() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserByEmail(email))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("User not found"))
                .verify();
    }

    @Test
    void changeCommunityRole_success() {
        UUID userId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setCommunityId(communityId);
        request.setNewRole(CommunityRole.MODERATOR);

        UserCommunity adminUserCommunity = new UserCommunity();
        adminUserCommunity.setUserRole(CommunityRole.ADMIN);

        UserCommunity targetUserCommunity = new UserCommunity();
        targetUserCommunity.setUserRole(CommunityRole.MEMBER);

        UserCommunityDTO userCommunityDTO = new UserCommunityDTO();

        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId))
                .thenReturn(Mono.just(adminUserCommunity));
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId))
                .thenReturn(Mono.just(targetUserCommunity));
        when(userCommunityRepository.save(any(UserCommunity.class))).thenReturn(Mono.just(targetUserCommunity));
        when(modelMapper.map(targetUserCommunity, UserCommunityDTO.class)).thenReturn(userCommunityDTO);

        StepVerifier.create(userService.changeCommunityRole(userId, Mono.just(request), authUserId))
                .expectNext(userCommunityDTO)
                .verifyComplete();

        verify(userCommunityRepository).save(argThat(uc -> uc.getUserRole() == CommunityRole.MODERATOR));
    }

    @Test
    void changeCommunityRole_notAdmin() {
        UUID userId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setCommunityId(communityId);
        request.setNewRole(CommunityRole.MODERATOR);

        UserCommunity nonAdminUserCommunity = new UserCommunity();
        nonAdminUserCommunity.setUserRole(CommunityRole.MEMBER);

        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId))
                .thenReturn(Mono.just(nonAdminUserCommunity));

        StepVerifier.create(userService.changeCommunityRole(userId, Mono.just(request), authUserId))
                .expectErrorMatches(throwable -> throwable instanceof NotAllowedException &&
                        throwable.getMessage().equals("Authenticated user isn't an administrator of specified group"))
                .verify();
    }

    @Test
    void changeCommunityRole_notMember() {
        UUID userId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setCommunityId(communityId);
        request.setNewRole(CommunityRole.MODERATOR);

        UserCommunity adminUserCommunity = new UserCommunity();
        adminUserCommunity.setUserRole(CommunityRole.ADMIN);

        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId))
                .thenReturn(Mono.just(adminUserCommunity));
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId))
                .thenReturn(Mono.empty());

        StepVerifier.create(userService.changeCommunityRole(userId, Mono.just(request), authUserId))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("User isn't a member of this group"))
                .verify();
    }

    @Test
    void validateEmailUnique_emailTaken() {
        String email = "taken@example.com";
        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.validateEmailUnique(email))
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        throwable.getMessage().equals("Email is already taken"))
                .verify();
    }

    @Test
    void validateEmailUnique_emailAvailable() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Mono.empty());

        StepVerifier.create(userService.validateEmailUnique(email))
                .verifyComplete();
    }

    @Test
    void validatePassword_correctPassword() {
        UUID userId = UUID.randomUUID();
        String rawPassword = "rawPass";
        User user = new User();
        user.setPassword("encodedPass");

        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(rawPassword, "encodedPass")).thenReturn(true);

        StepVerifier.create(userService.validatePassword(userId, rawPassword))
                .verifyComplete();
    }

    @Test
    void validatePassword_wrongPassword() {
        UUID userId = UUID.randomUUID();
        String rawPassword = "wrongPass";
        User user = new User();
        user.setPassword("encodedPass");

        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(rawPassword, "encodedPass")).thenReturn(false);

        StepVerifier.create(userService.validatePassword(userId, rawPassword))
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        throwable.getMessage().equals("Password mismatch"))
                .verify();
    }

    @Test
    void validatePassword_userNotFound() {
        UUID userId = UUID.randomUUID();
        String rawPassword = "pass";

        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.validatePassword(userId, rawPassword))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("User not found"))
                .verify();
    }
}
