package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRelationshipRepository;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserRelationshipServiceTest {

    @Mock
    private UserRelationshipRepository userRelationshipRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRelationshipService userRelationshipService;

    private UUID userId;
    private UUID authUserId;
    private User user;
    private User authUser;
    private UserRelationship userRelationship;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        authUserId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        authUser = new User();
        authUser.setId(authUserId);

        userRelationship = new UserRelationship();
        userRelationship.setRequester(user);
        userRelationship.setAcceptor(authUser);
        userRelationship.setType(RelationshipType.ACCEPTOR_PENDING);
    }

    @Test
    public void testGetFriendRequest_Success() {
        when(userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId)).thenReturn(Optional.of(userRelationship));

        UserRelationship foundRelationship = userRelationshipService.getFriendRequest(userId, authUserId);

        assertEquals(userRelationship, foundRelationship);
    }

    @Test
    public void testGetFriendRequest_NotFound() {
        when(userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userRelationshipService.getFriendRequest(userId, authUserId);
        });

        assertEquals("User relationship not found", exception.getMessage());
    }

    @Test
    public void testSendFriendRequest() {
        when(userService.getUserById(authUserId)).thenReturn(authUser);
        when(userService.getUserById(userId)).thenReturn(user);

        userRelationshipService.sendFriendRequest(userId, authUserId);
    }

    @Test
    public void testAcceptFriendRequest_Success() {
        when(userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId)).thenReturn(Optional.of(userRelationship));

        userRelationshipService.acceptFriendRequest(userId, authUserId);

        assertEquals(RelationshipType.FRIENDS, userRelationship.getType());
        verify(userRelationshipRepository).save(userRelationship);
    }

    @Test
    public void testAcceptFriendRequest_RequesterCannotAccept() {
        when(userRelationshipRepository.findByRequesterIdAndAcceptorId(authUserId, userId)).thenReturn(Optional.of(userRelationship));

        NotAllowedException exception = assertThrows(NotAllowedException.class, () -> {
            userRelationshipService.acceptFriendRequest(authUserId, userId);
        });

        assertEquals("Requester cannot accept the request", exception.getMessage());
    }

    @Test
    public void testRemoveFriendRequest() {
        when(userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId)).thenReturn(Optional.of(userRelationship));

        userRelationshipService.removeFriendRequest(userId, authUserId);

        verify(userRelationshipRepository).delete(userRelationship);
    }
}