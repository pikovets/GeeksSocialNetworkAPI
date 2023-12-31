package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.core.ErrorUtils;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserResponse;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;
import org.pikovets.GeeksSocialNetworkAPI.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final UserValidator userValidator;
    private final PostService postService;
    private final IAuthenticationFacade authenticationFacade;

    @Autowired
    public UserController(UserService userService,
                          ModelMapper modelMapper,
                          UserValidator userValidator,
                          PostService postService,
                          IAuthenticationFacade authenticationFacade) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
        this.postService = postService;
        this.authenticationFacade = authenticationFacade;
    }

    @Operation(
            summary = "Get all users",
            description = "Returns all registered users",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @GetMapping
    public ResponseEntity<UserResponse> getAllUsers() {
        return new ResponseEntity<>(
                new UserResponse(userService.getAllUsers().stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @Operation(
            summary = "Find user by ID",
            description = "Returns a single user",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of user to return"
                    )
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") UUID id) {
        return new ResponseEntity<>(convertToUserDTO(userService.getUserById(id)), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return new ResponseEntity<>(convertToUserDTO(userService.getUserById(authenticationFacade.getUserID())), HttpStatus.OK);
    }

    @Operation(
            summary = "Update an existing user",
            description = "Updates a specific user by id. If the updated email is already taken, a Bad Request error will be thrown",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of user to update"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "UserDTO",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
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
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PatchMapping
    public ResponseEntity<HttpStatus> updateUser(@RequestBody UserDTO userDTO, BindingResult bindingResult) {
        User existingUser = userService.getUserById(authenticationFacade.getUserID());
        User mergedUser = userService.mergeUsers(existingUser, convertToUser(userDTO));

        if (!mergedUser.getEmail().equals(existingUser.getEmail())) {
            userValidator.validate(mergedUser, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            ErrorUtils.returnBadRequestException(bindingResult);
        }

        userService.updateUser(mergedUser, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Delete user by ID",
            description = "Deletes an existing user",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of user to delete"
                    )
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteUser() {
        userService.deleteUser(authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Create post",
            description = "Creates a post on the wall of the specified user",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Post author id"
                    )
            },
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
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/{id}/wall")
    public ResponseEntity<HttpStatus> createPost(@RequestBody CreatePostRequest createRequest) {
        postService.createPost(authenticationFacade.getUserID(), createRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}