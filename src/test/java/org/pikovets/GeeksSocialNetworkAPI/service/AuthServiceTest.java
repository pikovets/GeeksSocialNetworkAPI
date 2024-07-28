package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pikovets.GeeksSocialNetworkAPI.dto.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.AuthDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private AuthService authService;

    private SignUpDTO signUpDTO;
    private AuthDTO authDTO;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authDTO = new AuthDTO();
        authDTO.setEmail("john.doe@gmail.com");
        authDTO.setPassword("T0Pko1PAtI2SuJq");

        signUpDTO = new SignUpDTO();
        signUpDTO.setFullName("John Doe");
        signUpDTO.setEmail("john.doe@gmail.com");
        signUpDTO.setPassword("T0Pko1PAtI2SuJq");

        user = new User();
        user.setId(UUID.fromString("aa991fb7-73df-4f84-85e3-be9b1d67300f"));
        user.setEmail("john.doe@gmail.com");
        user.setPassword("VDBQa28xUEF0STJTdUpx");
        user.setRole(Role.USER);
        user.setIsActive(true);
    }

    @Test
    void testRegisterUser_ShouldSaveUserAndProfile() {
        authService.registerUser(signUpDTO);

        verify(userRepository, times(1)).save(any(User.class));
        verify(profileService, times(1)).saveEmptyProfile(null);
    }


    @Test
    public void testLoginUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(any(User.class))).thenReturn("jwtToken");

        TokenResponse tokenResponse = authService.loginUser(authDTO);

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("jwtToken", tokenResponse.getToken());
    }

    @Test
    public void testLoginUser_IncorrectPassword() {
        doThrow(new BadRequestException("Incorrect username or password"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadRequestException.class, () -> authService.loginUser(authDTO));
    }
}