package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.ChangeRoleRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private User user;
    private UUID userId;
    private UUID communityId;
    private String userEmail;
    private UserCommunity userCommunity;
    private ChangeRoleRequest changeRoleRequest;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCommunityRepository userCommunityRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        communityId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail(userEmail);
        user.setPassword("VDBQa28xUEF0STJTdUpx");
        user.setRole(Role.USER);
        user.setIsActive(true);
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();
        assertEquals(users, result);
    }

    @Test
    public void testGetUserById() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);
        assertEquals(user, result);
    }

    @Test
    public void testGetUsersByName() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        userService.getUsersByName(user.getFirstName() + user.getLastName());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testGetUserByEmail() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(userEmail);
        assertEquals(user, result);
    }

    @Test
    public void testUpdateUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = new User();
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Doe");
        updatedUser.setEmail("jane.doe@example.com");

        userService.updateUser(updatedUser, userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(userId, savedUser.getId());
        assertEquals("Jane", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("jane.doe@example.com", savedUser.getEmail());
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void testDeleteUser_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        assertEquals("User not found", thrown.getMessage());
        verify(userRepository, times(0)).delete(any(User.class));
    }

    @Test
    public void testGetFriends() {
        User friend = new User();
        friend.setId(UUID.randomUUID());
        friend.setEmail("friend@gmail.com");

        UserRelationship relationship = new UserRelationship();
        relationship.setRequester(user);
        relationship.setAcceptor(friend);
        relationship.setType(RelationshipType.FRIENDS);

        user.setFriendshipsRequested(Set.of(relationship));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<User> friends = userService.getFriends(userId);

        assertEquals(List.of(friend), friends);
    }

    @Test
    public void testGetCommunities() {
        Community community = new Community();
        community.setId(communityId);

        UserCommunity userCommunity = new UserCommunity();
        userCommunity.setCommunity(community);
        user.setUserCommunities(Set.of(userCommunity));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<Community> communities = userService.getCommunities(userId);

        assertEquals(List.of(community), communities);
    }

    @Test
    public void testChangeCommunityRole() {
        User adminUser = new User();

        UserCommunity adminUserCommunity = new UserCommunity();
        adminUserCommunity.setUserRole(CommunityRole.ADMIN);
        adminUserCommunity.setCommunity(new Community());
        adminUserCommunity.getCommunity().setId(communityId);

        UserCommunity userCommunity = new UserCommunity();
        userCommunity.setUserRole(CommunityRole.MEMBER);
        userCommunity.setCommunity(adminUserCommunity.getCommunity());

        changeRoleRequest = new ChangeRoleRequest();
        changeRoleRequest.setCommunityId(communityId);
        changeRoleRequest.setNewRole(CommunityRole.MODERATOR);

        adminUser.setId(UUID.randomUUID());
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, adminUser.getId())).thenReturn(Optional.of(adminUserCommunity));
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId)).thenReturn(Optional.of(userCommunity));

        userService.changeCommunityRole(userId, changeRoleRequest, adminUser.getId());

        verify(userCommunityRepository, times(1)).findByCommunityIdAndUserId(communityId, adminUser.getId());
        verify(userCommunityRepository, times(1)).findByCommunityIdAndUserId(communityId, userId);
        assertEquals(CommunityRole.MODERATOR, userCommunity.getUserRole());
    }

    @Test
    public void testChangeCommunityRole_NotAdmin() {
        User nonAdminUser = new User();
        nonAdminUser.setId(UUID.randomUUID());

        UserCommunity nonAdminUserCommunity = new UserCommunity();
        nonAdminUserCommunity.setUserRole(CommunityRole.MEMBER);
        nonAdminUserCommunity.setCommunity(new Community());
        nonAdminUserCommunity.getCommunity().setId(communityId);

        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, nonAdminUser.getId())).thenReturn(Optional.of(nonAdminUserCommunity));

        changeRoleRequest = new ChangeRoleRequest();
        changeRoleRequest.setCommunityId(communityId);
        changeRoleRequest.setNewRole(CommunityRole.MODERATOR);

        NotAllowedException thrown = assertThrows(NotAllowedException.class, () -> {
            userService.changeCommunityRole(userId, changeRoleRequest, nonAdminUser.getId());
        });

        assertEquals("Authenticated user isn't an administrator of specified group", thrown.getMessage());
    }
}
