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
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/users")
@Tag(name = "User")
public class UserController {
    private final UserService userService;
    private final IAuthenticationFacade authenticationFacade;

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper, IAuthenticationFacade authenticationFacade) {
        this.userService = userService;
        this.authenticationFacade = authenticationFacade;
    }

    @Operation(summary = "Get all users", description = "Get all registered users", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping
    public Flux<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Get user", description = "Get user by id", parameters = {@Parameter(name = "id", description = "ID of user to return")}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/{id}")
    public Mono<UserDTO> getUserById(@PathVariable("id") UUID id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Get current user", description = "Get current user", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/me")
    public Mono<UserDTO> getCurrentUser() {
        return authenticationFacade.getUserID().flatMap(userService::getUserById);
    }

    @Operation(summary = "Delete current user", description = "Delete current user", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @DeleteMapping("/me")
    public Mono<Void> deleteCurrentUser() {
        return authenticationFacade.getUserID().flatMap(userService::deleteUser).then();
    }

    @Operation(summary = "Search user", description = "Search user by name", parameters = {@Parameter(name = "name", description = "User name", required = true),}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/searchByName")
    public Flux<UserDTO> getSearchedUsers(@RequestParam(value = "name") String name) {
        return userService.getUsersByName(name);
    }

    @Operation(summary = "Get current user friends", description = "Get current user friends", parameters = {@Parameter(name = "name", description = "User name", required = true),}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/me/getFriends")
    public Flux<UserDTO> getFriends() {
        return authenticationFacade.getUserID().flatMapMany(userService::getFriends);
    }

    @Operation(summary = "Get the friends of a specific user", description = "Get the friends of a specific user by user id", parameters = {@Parameter(name = "id", description = "User id", required = true),}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/{id}/getFriends")
    public Flux<UserDTO> getFriends(@PathVariable("id") UUID userId) {
        return userService.getFriends(userId);
    }

    @Operation(summary = "Get friend requests waiting for confirmation", description = "Get friend requests waiting for confirmation", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/me/getAcceptFriendRequests")
    public Flux<UserDTO> getAcceptFriendRequests() {
        return authenticationFacade.getUserID().flatMapMany(userService::getAcceptFriendRequests);
    }

    @Operation(summary = "Get current user communities", description = "Get current user communities", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/me/getCommunities")
    public Flux<CommunityDTO> getCommunities() {
        return authenticationFacade.getUserID().flatMapMany(userService::getCommunities);
    }

    @Operation(summary = "Get user communities", description = "Get specific user communities by user id", parameters = {@Parameter(name = "id", description = "User id", required = true),}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
        @GetMapping("/{id}/getCommunities")
    public Flux<CommunityDTO> getCommunities(@PathVariable("id") UUID userId) {
        return userService.getCommunities(userId);
    }

    @Operation(summary = "Change the user's role in community", description = "Change the user's role in a specific community by user id", parameters = {@Parameter(name = "id", description = "User id", required = true),}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Not Allowed", responseCode = "405", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "401", content = @Content(schema = @Schema(hidden = true)))})
    @PatchMapping("/{id}/changeRole")
    public Mono<Void> changeCommunityRole(@PathVariable("id") UUID userId, @RequestBody Mono<ChangeRoleRequest> changeRoleRequest) {
        return authenticationFacade.getUserID().flatMap(authUserId -> userService.changeCommunityRole(userId, changeRoleRequest, authUserId)).then();
    }
}