package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserResponse;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;
import org.pikovets.GeeksSocialNetworkAPI.core.ErrorUtils;
import org.pikovets.GeeksSocialNetworkAPI.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Tag(name = "User")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final UserValidator userValidator;
    private final PostService postService;

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper, UserValidator userValidator, PostService postService) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
        this.postService = postService;
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
                            responseCode= "404",
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
    @PatchMapping("/{id}")
    public ResponseEntity<HttpStatus> updateUser(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult,
                                                 @PathVariable("id") UUID id) {
        User user = convertToUser(userDTO);

        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            ErrorUtils.returnBadRequestException(bindingResult);
        }

        userService.updateUser(user, id);
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
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
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
    public ResponseEntity<HttpStatus> createPost(@PathVariable("id") UUID authorId, @RequestBody CreatePostRequest createRequest) {
        postService.createPost(authorId, createRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Get posts",
            description = "Gets posts of the specified user",
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
    @GetMapping("/{id}/wall")
    public ResponseEntity<PostResponse> getUserPosts(@PathVariable("id") UUID authorId){
        return new ResponseEntity<>(
                new PostResponse(userService.getUserPosts(authorId).stream().map(this::convertToPostDTO).toList()), HttpStatus.OK);
    }

    public PostDTO convertToPostDTO(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}