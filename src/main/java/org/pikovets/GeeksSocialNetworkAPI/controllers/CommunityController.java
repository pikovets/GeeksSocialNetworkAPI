package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pikovets.GeeksSocialNetworkAPI.core.ErrorUtils;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.*;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.security.AuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.CommunityService;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/communities")
@Tag(name = "Community")
public class CommunityController {
    private final AuthenticationFacade authenticationFacade;
    private final CommunityService communityService;
    private final PostService postService;

    @Autowired
    public CommunityController(AuthenticationFacade authenticationFacade, CommunityService communityService, PostService postService) {
        this.authenticationFacade = authenticationFacade;
        this.communityService = communityService;
        this.postService = postService;
    }

    @Operation(summary = "Get all communities", description = "Get all communities", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping
    public Flux<CommunityDTO> getAllCommunities() {
        return communityService.getAll();
    }

    @Operation(summary = "Get specific community", description = "Get specific community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/{id}")
    public Mono<CommunityDTO> getCommunity(@PathVariable("id") UUID communityId) {
        return communityService.getById(communityId);
    }

    @Operation(summary = "Get specific community profile", description = "Get specific community profile by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/{id}/getCommunityProfile")
    public Mono<CommunityProfileDTO> getCommunityProfile(@PathVariable("id") UUID communityId) {
        return communityService.getProfileById(communityId);
    }

    @Operation(summary = "Create community", description = "Create community", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @PostMapping
    public Mono<Void> createCommunity(@RequestBody Mono<CreateCommunityRequest> communityRequest) {
        return authenticationFacade.getUserID().flatMap(authUserId -> communityService.createCommunity(communityRequest, authUserId)).then();
    }

    @Operation(summary = "Delete community", description = "Delete community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @DeleteMapping("/{id}")
    public Mono<Void> deleteCommunity(@PathVariable("id") UUID communityId) {
        return authenticationFacade.getUserID().flatMap(authUserId -> communityService.deleteCommunityById(communityId, authUserId)).then();
    }

    @Operation(summary = "Join community", description = "Join community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @PostMapping("/{id}/join")
    public Mono<Void> joinCommunity(@PathVariable("id") UUID communityId) {
        return authenticationFacade.getUserID().flatMap(authUserId -> communityService.joinCommunity(communityId, authUserId)).then();
    }

    @Operation(summary = "Leave community", description = "Leave community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @DeleteMapping("/{id}/leave")
    public Mono<Void> leaveCommunity(@PathVariable("id") UUID communityId) {
        return authenticationFacade.getUserID().flatMap(authUserId -> communityService.leaveCommunity(communityId, authUserId)).then();
    }

    @Operation(summary = "Search community", description = "Search community by community name", parameters = {@Parameter(name = "name", description = "Community name", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/searchByName")
    public Flux<CommunityDTO> searchCommunityByName(@RequestParam(name = "name") String communityName) {
        return communityService.searchCommunityByName(communityName);
    }

    @Operation(summary = "Get the user's role in a specific community", description = "Get the user's role in a specific community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/{communityId}/getUserRole/{userId}")
    public Mono<CommunityRole> getUserRole(@PathVariable("communityId") UUID communityId, @PathVariable("userId") UUID userId) {
        return communityService.getCurrentUserRole(communityId, userId);
    }

    @Operation(summary = "Upload community post", parameters = {@Parameter(name = "communityId", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @PostMapping("/{communityId}/wall")
    public Mono<Void> uploadCommunityPost(@RequestBody Mono<CreatePostRequest> createPostRequest, @PathVariable("communityId") UUID communityId) {
        return authenticationFacade.getUserID().flatMap(authUserId -> postService.createPost(createPostRequest, authUserId, communityId)).then();
    }

    @PutMapping("/{communityId}")
    public Mono<Void> updateCommunity(@PathVariable("communityId") UUID communityId, @RequestBody Mono<CommunityUpdateRequest> communityUpdateDTOMono) {
        communityUpdateDTOMono.flatMap(communityUpdateRequest -> {
            BindingResult bindingResult = new BeanPropertyBindingResult(communityUpdateRequest, "communityUpdateDTO");
            if (bindingResult.hasErrors()) {
                ErrorUtils.returnBadRequestException(bindingResult);
            }
            return communityUpdateDTOMono;
        });
        return authenticationFacade.getUserID().flatMap(authUserId -> communityService.updateCommunity(communityUpdateDTOMono, communityId, authUserId)).then();
    }

    @Operation(summary = "Get community join requests", description = "Get community join requests for community moderation", parameters = {@Parameter(name = "communityId", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/{communityId}/communityJoinRequests")
    public Flux<UserDTO> getCommunityJoinRequests(@PathVariable(name = "communityId") UUID communityId) {
        return authenticationFacade.getUserID().flatMapMany(authUserId -> communityService.getCommunityJoinRequests(communityId, authUserId));
    }

    @PutMapping("/{communityId}/acceptJoinCommunityRequest/{userId}")
    public Mono<Void> acceptJoinCommunityRequest(@PathVariable("communityId") UUID communityId, @PathVariable("userId") UUID userId) {
        return communityService.addMember(communityId, userId).then();
    }

    @DeleteMapping("/{communityId}/cancelJoinCommunityRequest/{userId}")
    public Mono<Void> cancelJoinCommunityRequest(@PathVariable("communityId") UUID communityId, @PathVariable("userId") UUID userId) {
        return authenticationFacade.getUserID().flatMap(authUserId -> communityService.deleteJoinCommunityRequest(communityId, userId, authUserId));
    }
}