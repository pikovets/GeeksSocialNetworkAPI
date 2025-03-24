package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CreateCommunityRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.UserCommunityDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommunityServiceTest {
    private UUID userId;
    private UUID communityId;
    private User user;
    private Community community;
    private CreateCommunityRequest communityRequest;
    private UserCommunity userCommunity;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCommunityRepository userCommunityRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommunityService communityService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        communityId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        community = new Community();
        community.setId(communityId);

        userCommunity = new UserCommunity();
        userCommunity.setUser(user);
        userCommunity.setCommunity(community);
        userCommunity.setUserRole(CommunityRole.MEMBER);

        communityRequest = new CreateCommunityRequest();
        communityRequest.setName("Test");
    }

    @Test
    public void testGetAll() {
        when(communityRepository.findAll()).thenReturn(List.of(community));

        List<Community> result = communityService.getAll();

        assertEquals(List.of(community), result);
        verify(communityRepository, times(1)).findAll();
    }

    @Test
    public void testGetById_Success() {
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        Community result = communityService.getById(communityId);

        assertEquals(community, result);
        verify(communityRepository, times(1)).findById(communityId);
    }

    @Test
    public void testGetById_NotFoundCommunity() {
        when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> communityService.getById(communityId));

        assertEquals("Community not found", thrown.getMessage());
        verify(communityRepository, times(1)).findById(communityId);
    }

    @Test
    public void testCreateCommunity() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        communityService.createCommunity(communityRequest, userId);

        verify(communityRepository, times(1)).save(any(Community.class));
    }

    @Test
    public void testAddMember() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        communityService.addMember(communityId, userId);

        verify(userCommunityRepository, times(1)).save(any(UserCommunity.class));
    }

    @Test
    void testAddMemberCommunity_NotFound() {
        when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            communityService.addMember(communityId, userId);
        });

        assertEquals("Community not found", exception.getMessage());
    }

    @Test
    void testLeaveCommunity() {
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId))
                .thenReturn(Optional.of(userCommunity));

        communityService.leaveCommunity(communityId, userId);

        verify(userCommunityRepository, times(1)).delete(userCommunity);
    }

    @Test
    void testLeaveCommunity_UserNotFoundInCommunity() {
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            communityService.leaveCommunity(communityId, userId);
        });

        assertEquals("User not found in community", exception.getMessage());
    }

    @Test
    void testGetCurrentUserRole() {
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId))
                .thenReturn(Optional.of(userCommunity));

        CommunityRole role = communityService.getCurrentUserRole(communityId, userId);

        assertEquals(CommunityRole.MEMBER, role);
    }

    @Test
    void testGetCurrentUserRole_UserNotInCommunity() {
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            communityService.getCurrentUserRole(communityId, userId);
        });

        assertEquals("User not found in community", exception.getMessage());
    }
}
