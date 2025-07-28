package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pikovets.GeeksSocialNetworkAPI.dto.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.AuthDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.JwtUtils;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtils jwtUtils;
    private ProfileService profileService;
    private TransactionalOperator transactionalOperator;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtils = mock(JwtUtils.class);
        profileService = mock(ProfileService.class);
        transactionalOperator = mock(TransactionalOperator.class);

        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService = new AuthService(userRepository, passwordEncoder, jwtUtils, profileService, transactionalOperator);
    }

    @Test
    void registerUser_success() {
        UUID userId = UUID.randomUUID();
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setFullName("John Doe");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("pass");

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return Mono.just(user);
        });
        when(profileService.saveEmptyProfile(userId)).thenReturn(Mono.empty());

        Mono<Void> result = authService.registerUser(Mono.just(signUpRequest));

        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assert savedUser.getFirstName().equals("John");
        assert savedUser.getLastName().equals("Doe");
        assert savedUser.getEmail().equals("test@example.com");
        assert savedUser.getPassword().equals("encodedPass");
        assert savedUser.getRole() == Role.USER;
        assert savedUser.getIsActive();
    }

    @Test
    void loginUser_success() {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("test@example.com");
        authDTO.setPassword("pass");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("pass", "encodedPass")).thenReturn(true);
        when(jwtUtils.generateToken(any(Mono.class))).thenReturn(Mono.just("token123"));

        Mono<TokenResponse> result = authService.loginUser(Mono.just(authDTO));

        StepVerifier.create(result)
                .expectNextMatches(tokenResponse -> tokenResponse.getToken().equals("token123"))
                .verifyComplete();
    }

    @Test
    void loginUser_incorrectPassword() {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("test@example.com");
        authDTO.setPassword("wrongpass");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("wrongpass", "encodedPass")).thenReturn(false);

        Mono<TokenResponse> result = authService.loginUser(Mono.just(authDTO));

        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void loginUser_userNotFound() {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("unknown@example.com");
        authDTO.setPassword("pass");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Mono.empty());

        Mono<TokenResponse> result = authService.loginUser(Mono.just(authDTO));

        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }
}
