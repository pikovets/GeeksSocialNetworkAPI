package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pikovets.GeeksSocialNetworkAPI.dto.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.AuthDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Role;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private AuthService authService;

    @Test
    public void testRegisterUser() {
        SignUpDTO signUpDTO = new SignUpDTO("John Doe", "john@example.com", "password123");

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> authService.registerUser(signUpDTO));

        Mockito.verify(userRepository, Mockito.times(1)).save(any());
        Mockito.verify(profileService, Mockito.times(1)).saveEmptyProfile(any());
    }

    @Test
    public void testLoginUser() {
        AuthDTO authDTO = new AuthDTO("john@example.com", "password123");
        User user = new User(UUID.randomUUID(), "John", "Doe", "john@example.com", "encodedPassword", true, Role.USER, Set.of());

        when(userRepository.findByEmail(authDTO.getEmail())).thenReturn(java.util.Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtUtils.generateToken(any())).thenReturn("mockedToken");

        TokenResponse tokenResponse = authService.loginUser(authDTO);

        assertNotNull(tokenResponse);
        assertEquals("mockedToken", tokenResponse.getToken());
    }

    @Test
    public void testLoginUser_UserNotFound() {
        AuthDTO authDTO = new AuthDTO("nonexistent@example.com", "password123");

        when(userRepository.findByEmail(authDTO.getEmail())).thenReturn(java.util.Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.loginUser(authDTO));
    }

    @Test
    public void testLoginUser_IncorrectCredentials() {
        AuthDTO authDTO = new AuthDTO("john@example.com", "wrongPassword");
        User user = new User(UUID.randomUUID(), "John", "Doe", "john@example.com", "encodedPassword", true, Role.USER, Set.of());

        when(userRepository.findByEmail(authDTO.getEmail())).thenReturn(java.util.Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Authentication failed"));

        assertThrows(BadRequestException.class, () -> authService.loginUser(authDTO));
    }
}