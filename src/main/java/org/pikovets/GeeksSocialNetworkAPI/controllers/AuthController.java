package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.core.ErrorUtils;
import org.pikovets.GeeksSocialNetworkAPI.dto.auth.LoginRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.auth.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.service.AuthService;
import org.pikovets.GeeksSocialNetworkAPI.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {
    private final AuthService authService;
    private final UserValidator userValidator;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthController(AuthService authService, UserValidator userValidator, ModelMapper modelMapper) {
        this.authService = authService;
        this.userValidator = userValidator;
        this.modelMapper = modelMapper;
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
    @PostMapping("/register")
    public ResponseEntity<HttpStatus> registerUser(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
        User user = convertToUser(userDTO);

        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            ErrorUtils.returnBadRequestException(bindingResult);
        }

        authService.registerUser(user);

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
                    )
            }
    )
    @GetMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        TokenResponse jwtResponse = authService.loginUser(loginRequest);
        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }

    public User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}