package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship.UserRelationshipDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.UserRelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/userRelations")
@Tag(name = "UserRelationship")
public class UserRelationshipController {
    private final UserRelationshipService userRelationshipService;
    private final IAuthenticationFacade authenticationFacade;

    @Autowired
    public UserRelationshipController(UserRelationshipService userRelationshipService, IAuthenticationFacade authenticationFacade) {
        this.userRelationshipService = userRelationshipService;
        this.authenticationFacade = authenticationFacade;
    }

    @Operation(summary = "Get friend request", description = "Get friend request by user id", parameters = {@Parameter(name = "userId", description = "User id", required = true),}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/getFriendRequest")
    public ResponseEntity<Mono<UserRelationshipDTO>> getFriendRequest(@RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(userRelationshipService.getFriendRequest(userId, authenticationFacade.getUserID()), HttpStatus.OK);
    }

    @Operation(summary = "Send friend request", description = "Send friend request by user id. The request will be pending acceptance by another user", parameters = {@Parameter(name = "userId", description = "User id", required = true),}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @PostMapping("/sendFriendRequest")
    public ResponseEntity<HttpStatus> sendFriendRequest(@RequestParam("userId") UUID userId) {
        userRelationshipService.sendFriendRequest(userId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Accept friend request", description = "Accept friend request by user id", parameters = {@Parameter(name = "userId", description = "User id", required = true),}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Not Allowed", responseCode = "405", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @PatchMapping("/acceptFriendRequest")
    public ResponseEntity<HttpStatus> acceptFriendRequest(@RequestParam("userId") UUID userId) {
        userRelationshipService.acceptFriendRequest(userId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Operation(summary = "Delete friend request", description = "Delete friend request by user id", parameters = {@Parameter(name = "userId", description = "User id", required = true),}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @DeleteMapping("/removeFriendRequest")
    public ResponseEntity<HttpStatus> removeFriendRequest(@RequestParam("userId") UUID userId) {
        userRelationshipService.removeFriendRequest(userId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}