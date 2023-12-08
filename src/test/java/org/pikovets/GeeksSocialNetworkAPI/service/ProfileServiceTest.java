package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.ProfileRepository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProfileService profileService;

    @Test
    public void testGetProfileByUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Profile mockProfile = new Profile();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(mockProfile));

        // Act
        Profile resultProfile = profileService.getProfileByUserId(userId);

        // Assert
        assertNotNull(resultProfile);
        assertEquals(mockProfile, resultProfile);
    }

    @Test
    public void testGetProfileByUserIdNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(NotFoundException.class, () -> profileService.getProfileByUserId(userId));
    }

    @Test
    public void testSaveEmptyProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        when(userService.getUserById(userId)).thenReturn(mockUser);

        // Act
        profileService.saveEmptyProfile(userId);

        // Assert
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    public void testUpdate() {
        UUID userId = UUID.randomUUID();
        User updatedUser = new User();
        Profile updatedProfile = new Profile();
        updatedProfile.setBio("Updated Bio");
        updatedProfile.setSex("Male");
        updatedProfile.setAddress("Updated Address");
        updatedProfile.setBirthday(new Date());

        User existingUser = new User();
        existingUser.setFirstName("existingUser");
        Profile existingProfile = new Profile();
        existingProfile.setBio("Existing Bio");
        existingProfile.setSex("Female");
        existingProfile.setAddress("Existing Address");
        existingProfile.setBirthday(new Date());

        Mockito.when(userService.getUserById(userId)).thenReturn(existingUser);
        Mockito.when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existingProfile));

        profileService.update(updatedUser, updatedProfile, userId);

        Mockito.verify(userService).updateUser(updatedUser, userId);
        Mockito.verify(profileRepository).save(updatedProfile);
    }

    @Test
    public void testMergeProfiles() {
        // Arrange
        Profile profileToBeUpdated = new Profile();
        Profile newProfile = new Profile();
        newProfile.setBio("New Bio");
        newProfile.setBirthday(new Date());
        newProfile.setSex("New Sex");
        newProfile.setAddress("New Address");

        // Act
        profileService.mergeProfiles(profileToBeUpdated, newProfile);

        // Assert
        assertEquals("New Bio", profileToBeUpdated.getBio());
        assertEquals("New Sex", profileToBeUpdated.getSex());
        assertEquals("New Address", profileToBeUpdated.getAddress());
        assertEquals(newProfile.getBirthday(), profileToBeUpdated.getBirthday());
    }
}