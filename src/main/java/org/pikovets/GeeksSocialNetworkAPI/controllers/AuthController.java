package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.core.ErrorUtils;
import org.pikovets.GeeksSocialNetworkAPI.dto.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.AuthDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.service.AuthService;
import org.pikovets.GeeksSocialNetworkAPI.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {
    private final AuthService authService;
    private final UserValidator userValidator;

    @Autowired
    public AuthController(AuthService authService, UserValidator userValidator) {
        this.authService = authService;
        this.userValidator = userValidator;
    }

    @Operation(
            summary = "Register a new user",
            description = "Registers a new user. If the email is already taken, a Bad Request error will be thrown",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Bad Request",
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    )
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<HttpStatus> registerUser(@RequestBody @Valid SignUpDTO signUpDTO, BindingResult bindingResult) {
        userValidator.validate(signUpDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            ErrorUtils.returnBadRequestException(bindingResult);
        }

        authService.registerUser(signUpDTO);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
            summary = "Login",
            description = "Login and returns a jwt token. If the user with this login does not exist, the error Not Found will be thrown",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Bad Request",
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
            }
    )
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@RequestBody AuthDTO authDTO) {
        TokenResponse jwtResponse = authService.loginUser(authDTO);
        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }
}