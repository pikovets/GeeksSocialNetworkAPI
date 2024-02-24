package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserResponse;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
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

    @Operation(summary = "Get all users", description = "Returns all registered users", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403")})
    @GetMapping
    public ResponseEntity<UserResponse> getAllUsers() {
        return new ResponseEntity<>(new UserResponse(userService.getAllUsers().stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @Operation(summary = "Find user by ID", description = "Returns a single user", parameters = {@Parameter(name = "id", description = "ID of user to return")}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403")})
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") UUID id) {
        return new ResponseEntity<>(convertToUserDTO(userService.getUserById(id)), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return new ResponseEntity<>(convertToUserDTO(userService.getUserById(authenticationFacade.getUserID())), HttpStatus.OK);
    }

    @Operation(summary = "Delete user by ID", description = "Deletes an existing user", parameters = {@Parameter(name = "id", description = "ID of user to delete")}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403")})
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteUser() {
        userService.deleteUser(authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/searchByName")
    public ResponseEntity<UserResponse> getSearchedUsers(@RequestParam(value = "name") String name) {
        return new ResponseEntity<>(new UserResponse(userService.getUsersByName(name).stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @GetMapping("/me/getFriends")
    public ResponseEntity<UserResponse> getFriends() {
        return new ResponseEntity<>(new UserResponse(userService.getFriends(authenticationFacade.getUserID()).stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @GetMapping("/{id}/getFriends")
    public ResponseEntity<UserResponse> getFriends(@PathVariable("id") UUID userId) {
        return new ResponseEntity<>(new UserResponse(userService.getFriends(userId).stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    @GetMapping("/me/getAcceptFriendRequests")
    public ResponseEntity<UserResponse> getAcceptFriendRequests() {
        return new ResponseEntity<>(new UserResponse(userService.getAcceptFriendRequests(authenticationFacade.getUserID()).stream().map(this::convertToUserDTO).toList()), HttpStatus.OK);
    }

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
}