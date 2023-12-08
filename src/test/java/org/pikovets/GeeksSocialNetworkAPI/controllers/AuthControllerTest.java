package org.pikovets.GeeksSocialNetworkAPI.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pikovets.GeeksSocialNetworkAPI.dto.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.AuthDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.service.AuthService;
import org.pikovets.GeeksSocialNetworkAPI.validator.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private AuthController authController;

    @Test
    void testRegisterUser_Success() {
        SignUpDTO signUpDTO = new SignUpDTO();
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<HttpStatus> responseEntity = authController.registerUser(signUpDTO, bindingResult);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        verify(authService, times(1)).registerUser(signUpDTO);
    }

    @Test
    void testRegisterUser_BadRequest() {
        SignUpDTO signUpDTO = new SignUpDTO();
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authController.registerUser(signUpDTO, bindingResult));

        verify(authService, never()).registerUser(signUpDTO);
    }

    @Test
    void testLoginUser_Success() {
        AuthDTO authDTO = new AuthDTO();
        TokenResponse tokenResponse = new TokenResponse();

        when(authService.loginUser(authDTO)).thenReturn(tokenResponse);

        ResponseEntity<TokenResponse> responseEntity = authController.loginUser(authDTO);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(tokenResponse, responseEntity.getBody());
    }
}