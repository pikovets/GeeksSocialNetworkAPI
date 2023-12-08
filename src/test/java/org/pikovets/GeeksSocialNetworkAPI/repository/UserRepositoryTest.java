package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pikovets.GeeksSocialNetworkAPI.model.Role;
import org.pikovets.GeeksSocialNetworkAPI.model.User;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {
    @Mock
    private UserRepository userRepositoryMock;

    @InjectMocks
    private UserRepository userRepository;

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Arrange
        String testEmail = "test@gmail.com";
        User user = new User(UUID.randomUUID(), "Test", "Test", testEmail, "test", true, Role.USER, Set.of());

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userRepository.findByEmail(testEmail);

        // Assert
        verify(userRepositoryMock, times(1)).findByEmail(testEmail);
        assertEquals(user, result.orElse(null));
    }

    @Test
    void findByEmail_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        String userEmail = "nonexistent@example.com";

        when(userRepositoryMock.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findByEmail(userEmail);

        // Assert
        verify(userRepositoryMock, times(1)).findByEmail(userEmail);
        assertFalse(result.isPresent());
    }
}
