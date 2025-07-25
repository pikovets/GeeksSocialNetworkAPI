package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pikovets.GeeksSocialNetworkAPI.dto.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.AuthDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.service.AuthService;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Registers a new user. If the email is already taken, a Bad Request error will be thrown",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Bad Request",
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    )
            }
    )
    @PostMapping("/signup")
    public Mono<Void> registerUser(@RequestBody Mono<SignUpRequest> signUpDTOMono) {
        return signUpDTOMono
                .flatMap(signUpRequest ->
                        userService.validateEmailUnique(signUpRequest.getEmail())
                                .then(authService.registerUser(Mono.just(signUpRequest))));
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
    public Mono<TokenResponse> loginUser(@RequestBody Mono<AuthDTO> authDTOMono) {
        return authService.loginUser(authDTOMono);
    }
}