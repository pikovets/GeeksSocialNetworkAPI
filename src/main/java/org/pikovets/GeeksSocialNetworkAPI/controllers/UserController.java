package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.ChangeRoleRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserResponse;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/users")
@Tag(name = "User")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final IAuthenticationFacade authenticationFacade;

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper, IAuthenticationFacade authenticationFacade) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Operation(summary = "Get all users", description = "Get all registered users", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403")})
    @GetMapping
    public ResponseEntity<UserResponse> getAllUsers() {
        return new ResponseEntity<>(new UserResponse(userService.getAllUsers().stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @Operation(summary = "Get specific user", description = "Get user by id", parameters = {@Parameter(name = "id", description = "ID of user to return")}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403")})
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") UUID id) {
        return new ResponseEntity<>(convertToUserDTO(userService.getUserById(id)), HttpStatus.OK);
    }

    @Operation(
            summary = "Get current user",
            description = "Get current user",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return new ResponseEntity<>(convertToUserDTO(userService.getUserById(authenticationFacade.getUserID())), HttpStatus.OK);
    }

    @Operation(summary = "Delete current user", description = "Delete current user", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403")})
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteUser() {
        userService.deleteUser(authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Search user",
            description = "Search user by name",
            parameters = {
                    @Parameter(
                            name = "name",
                            description = "User name",
                            required = true
                    ),
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/searchByName")
    public ResponseEntity<UserResponse> getSearchedUsers(@RequestParam(value = "name") String name) {
        return new ResponseEntity<>(new UserResponse(userService.getUsersByName(name).stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @Operation(
            summary = "Get current user friends",
            description = "Get current user friends",
            parameters = {
                    @Parameter(
                            name = "name",
                            description = "User name",
                            required = true
                    ),
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/me/getFriends")
    public ResponseEntity<UserResponse> getFriends() {
        return new ResponseEntity<>(new UserResponse(userService.getFriends(authenticationFacade.getUserID()).stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @Operation(
            summary = "Get the friends of a specific user",
            description = "Get the friends of a specific user by user id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "User id",
                            required = true
                    ),
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}/getFriends")
    public ResponseEntity<UserResponse> getFriends(@PathVariable("id") UUID userId) {
        return new ResponseEntity<>(new UserResponse(userService.getFriends(userId).stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @Operation(
            summary = "Get friend requests waiting for confirmation",
            description = "Get friend requests waiting for confirmation",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/me/getAcceptFriendRequests")
    public ResponseEntity<UserResponse> getAcceptFriendRequests() {
        return new ResponseEntity<>(new UserResponse(userService.getAcceptFriendRequests(authenticationFacade.getUserID()).stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @Operation(
            summary = "Get current user communities",
            description = "Get current user communities",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/me/getCommunities")
    public ResponseEntity<CommunityResponse> getCommunities() {
        return new ResponseEntity<>(new CommunityResponse(userService.getCommunities(authenticationFacade.getUserID()).stream().map(this::convertToCommunityDTO).toList()), HttpStatus.OK);
    }

    @Operation(
            summary = "Get specific user communities",
            description = "Get specific user communities by user id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "User id",
                            required = true
                    ),
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}/getCommunities")
    public ResponseEntity<CommunityResponse> getCommunities(@PathVariable("id") UUID userId) {
        return new ResponseEntity<>(new CommunityResponse(userService.getCommunities(userId).stream().map(this::convertToCommunityDTO).toList()), HttpStatus.OK);
    }

    @Operation(
            summary = "Change the user's role in a specific community",
            description = "Change the user's role in a specific community by user id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "User id",
                            required = true
                    ),
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
                            description = "Not Allowed",
                            responseCode = "405",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @PatchMapping("/{id}/changeRole")
    public ResponseEntity<HttpStatus> changeCommunityRole(@PathVariable("id") UUID userId, @RequestBody ChangeRoleRequest changeRoleRequest) {
        userService.changeCommunityRole(userId, changeRoleRequest, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public CommunityDTO convertToCommunityDTO(Community community) {
        return modelMapper.map(community, CommunityDTO.class);
    }
}