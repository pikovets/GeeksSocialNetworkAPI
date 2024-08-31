package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.UserProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserUpdateDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.ProfileRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ProfileServiceTest {

    private UUID userId;
    private User user;
    private Profile profile;
    private UserUpdateDTO userUpdateDTO;
    private Profile updatedProfile;
    private UserProfileDTO userProfileDTO;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProfileService profileService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setFirstName("First name");
        user.setLastName("Last name");

        profile = new Profile();
        profile.setUser(user);
        profile.setJoinDate(new Date());

        userUpdateDTO = new UserUpdateDTO();

        updatedProfile = new Profile();
        updatedProfile.setSex("Male");

        userProfileDTO = new UserProfileDTO();
        userProfileDTO.setUser(userUpdateDTO);
        userProfileDTO.setProfile(updatedProfile);
    }

    @Test
    public void testGetProfileByUserId_Success() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        Profile result = profileService.getProfileByUserId(userId);

        assertEquals(result, profile);
        verify(profileRepository, times(1)).findByUserId(userId);
    }

    @Test
    public void testGetProfileByUserId_UserNotFound() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> profileService.getProfileByUserId(userId));

        assertEquals("User not found", thrown.getMessage());
        verify(profileRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testSaveEmptyProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        profileService.saveEmptyProfile(userId);

        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    public void testUpdateUser_Success() {
        when(userService.getUserById(userId)).thenReturn(user);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        profileService.updateUser(userProfileDTO, userId);

        verify(userService, times(1)).getUserById(userId);
        verify(profileRepository, times(1)).findByUserId(userId);
        verify(userRepository, times(1)).save(user);
        verify(profileRepository, times(1)).save(updatedProfile);
    }
}