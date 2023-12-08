package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    @Spy
    private UserService userService;

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Arrange
        List<User> userList = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(userList);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(userList, result);
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User expectedUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertEquals(expectedUser, result);
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
    }

    // Similarly, you can write tests for other methods in UserService
    // ...

    @Test
    void updateUser_ShouldUpdateUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User updatedUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        // Act
        userService.updateUser(updatedUser, userId);

        // Assert
        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void deleteUser_WithInvalidId_ShouldThrowNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
    }
}