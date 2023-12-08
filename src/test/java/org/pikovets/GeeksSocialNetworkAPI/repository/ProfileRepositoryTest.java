package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileRepositoryTest {
    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserService userService;

    @Test
    void findByUserId_ReturnsProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Profile mockProfile = new Profile();
        mockProfile.setUser(userService.getUserById(userId));

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(mockProfile));

        // Act
        Optional<Profile> result = profileRepository.findByUserId(userId);

        // Assert
        assertEquals(mockProfile, result.orElse(null));
    }

    @Test
    void findByUserId_UserNotFound_ReturnsEmptyOptional() {
        // Arrange
        UUID userId = UUID.randomUUID();

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act
        Optional<Profile> result = profileRepository.findByUserId(userId);

        // Assert
        assertEquals(Optional.empty(), result);
    }
}