package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship.UserRelationshipDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRelationshipRepository;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class UserRelationshipServiceTest {
    private UserRelationshipRepository userRelationshipRepository;
    private ModelMapper modelMapper;
    private TransactionalOperator transactionalOperator;

    private UserRelationshipService userRelationshipService;

    @BeforeEach
    void setUp() {
        userRelationshipRepository = mock(UserRelationshipRepository.class);
        modelMapper = mock(ModelMapper.class);
        transactionalOperator = mock(TransactionalOperator.class);

        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userRelationshipService = new UserRelationshipService(
                userRelationshipRepository,
                modelMapper,
                transactionalOperator
        );
    }

    @Test
    void getFriendRequest_found() {
        UUID userId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();

        UserRelationship relationship = new UserRelationship(userId, authUserId, RelationshipType.ACCEPTOR_PENDING);
        UserRelationshipDTO dto = new UserRelationshipDTO();

        when(userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId)).thenReturn(Mono.just(relationship));
        when(modelMapper.map(relationship, UserRelationshipDTO.class)).thenReturn(dto);

        StepVerifier.create(userRelationshipService.getFriendRequest(userId, authUserId))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void getFriendRequest_notFound() {
        UUID userId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();

        when(userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId)).thenReturn(Mono.empty());

        StepVerifier.create(userRelationshipService.getFriendRequest(userId, authUserId))
                .expectErrorMatches(e -> e instanceof NotFoundException && e.getMessage().equals("User relationship not found"))
                .verify();
    }

    @Test
    void sendFriendRequest_success() {
        UUID userId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();

        UserRelationship savedRelationship = new UserRelationship(authUserId, userId, RelationshipType.ACCEPTOR_PENDING);
        UserRelationshipDTO dto = new UserRelationshipDTO();

        when(userRelationshipRepository.save(any(UserRelationship.class))).thenReturn(Mono.just(savedRelationship));
        when(modelMapper.map(savedRelationship, UserRelationshipDTO.class)).thenReturn(dto);

        StepVerifier.create(userRelationshipService.sendFriendRequest(userId, authUserId))
                .expectNext(dto)
                .verifyComplete();

        verify(userRelationshipRepository).save(argThat(rel ->
                rel.getRequesterId().equals(authUserId) &&
                        rel.getAcceptorId().equals(userId) &&
                        rel.getType() == RelationshipType.ACCEPTOR_PENDING
        ));
    }

    @Test
    void acceptFriendRequest_success() {
        UUID userId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();

        UserRelationship relationship = new UserRelationship(userId, authUserId, RelationshipType.ACCEPTOR_PENDING);

        when(userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId)).thenReturn(Mono.just(relationship));
        when(userRelationshipRepository.updateRelationshipType(RelationshipType.FRIENDS.toString(), userId, authUserId)).thenReturn(Mono.empty());

        StepVerifier.create(userRelationshipService.acceptFriendRequest(userId, authUserId))
                .verifyComplete();
    }

    @Test
    void acceptFriendRequest_notFound() {
        UUID userId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();

        when(userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId)).thenReturn(Mono.empty());

        StepVerifier.create(userRelationshipService.acceptFriendRequest(userId, authUserId))
                .verifyComplete();
    }

    @Test
    void removeFriendRequest_success() {
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        when(userRelationshipRepository.deleteByRequesterIdAndAcceptorId(firstUserId, secondUserId)).thenReturn(Mono.empty());

        StepVerifier.create(userRelationshipService.removeFriendRequest(firstUserId, secondUserId))
                .verifyComplete();
    }
}
