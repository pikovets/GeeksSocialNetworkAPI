package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.core.ErrorUtils;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.*;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserResponse;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.security.AuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.CommunityService;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/communities")
@Tag(name = "Community")
public class CommunityController {
    private final AuthenticationFacade authenticationFacade;
    private final CommunityService communityService;
    private final PostService postService;
    private final ModelMapper modelMapper;

    @Autowired
    public CommunityController(AuthenticationFacade authenticationFacade, CommunityService communityService, PostService postService, ModelMapper modelMapper) {
        this.authenticationFacade = authenticationFacade;
        this.communityService = communityService;
        this.postService = postService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Get all communities", description = "Get all communities", responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping
    public ResponseEntity<CommunityResponse> getAllCommunities() {
        return new ResponseEntity<>(new CommunityResponse(communityService.getAll().stream().map(this::convertToCommunityDTO).toList()), HttpStatus.OK);
    }

    @Operation(summary = "Get specific community", description = "Get specific community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/{id}")
    public ResponseEntity<CommunityDTO> getCommunity(@PathVariable("id") UUID communityId) {
        return new ResponseEntity<>(convertToCommunityDTO(communityService.getById(communityId)), HttpStatus.OK);
    }

    @Operation(summary = "Get specific community profile", description = "Get specific community profile by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/{id}/getCommunityProfile")
    public ResponseEntity<CommunityProfileDTO> getCommunityProfile(@PathVariable("id") UUID communityId) {
        return new ResponseEntity<>(convertToCommunityProfileDTO(communityService.getById(communityId)), HttpStatus.OK);
    }

    @Operation(summary = "Create community", description = "Create community", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @PostMapping
    public ResponseEntity<HttpStatus> createCommunity(@RequestBody CreateCommunityRequest communityRequest) {
        communityService.createCommunity(communityRequest, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Delete community", description = "Delete community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCommunity(@PathVariable("id") UUID id) {
        communityService.deleteCommunityById(id, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Join community", description = "Join community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @PostMapping("/{id}/join")
    public ResponseEntity<HttpStatus> joinCommunity(@PathVariable("id") UUID communityId) {
        communityService.addMember(communityId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Leave community", description = "Leave community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Not Found", responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorObject.class))), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<HttpStatus> leaveCommunity(@PathVariable("id") UUID communityId) {
        communityService.leaveCommunity(communityId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Search community", description = "Search community by community name", parameters = {@Parameter(name = "name", description = "Community name", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/searchByName")
    public ResponseEntity<CommunityResponse> searchCommunityByName(@RequestParam(name = "name") String communityName) {
        return new ResponseEntity<>(new CommunityResponse(communityService.searchCommunityByName(communityName).stream().map(this::convertToCommunityDTO).toList()), HttpStatus.OK);
    }

    @Operation(summary = "Get the user's role in a specific community", description = "Get the user's role in a specific community by community id", parameters = {@Parameter(name = "id", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/{communityId}/getUserRole/{userId}")
    public ResponseEntity<CommunityRoleResponse> getUserRole(@PathVariable("communityId") UUID communityId, @PathVariable("userId") UUID userId) {
        return new ResponseEntity<>(new CommunityRoleResponse(communityService.getCurrentUserRole(communityId, userId)), HttpStatus.OK);
    }

    @Operation(summary = "Upload community post", parameters = {@Parameter(name = "communityId", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @PostMapping("/{communityId}/wall")
    public ResponseEntity<HttpStatus> uploadCommunityPost(@RequestBody CreatePostRequest createPostRequest, @PathVariable("communityId") UUID communityId) {

        postService.createPost(createPostRequest, authenticationFacade.getUserID(), communityId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{communityId}")
    public ResponseEntity<HttpStatus> updateCommunity(@PathVariable("communityId") UUID communityId, @RequestBody @Valid CommunityUpdateDTO communityUpdateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ErrorUtils.returnBadRequestException(bindingResult);
        }
        communityService.updateCommunity(communityUpdateDTO, communityId, authenticationFacade.getUserID());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get community join requests", description = "Get community join requests for community moderation", parameters = {@Parameter(name = "communityId", description = "Community id", required = true)}, responses = {@ApiResponse(description = "Success", responseCode = "200"), @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403", content = @Content)})
    @GetMapping("/{communityId}/communityJoinRequests")
    public ResponseEntity<UserResponse> getCommunityJoinRequests(@PathVariable(name = "communityId") UUID communityId) {
        return new ResponseEntity<>(new UserResponse(mapUsersToUserDTOs(communityService.getCommunityJoinRequests(communityId)).stream().toList()), HttpStatus.OK);
    }

    @PostMapping("/{communityId}/sendJoinCommunityRequest")
    public ResponseEntity<HttpStatus> sendJoinCommunityRequest(@PathVariable("communityId") UUID communityId) {
        communityService.sendJoinCommunityRequest(communityId, authenticationFacade.getUserID());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{communityId}/acceptJoinCommunityRequest/{userId}")
    public ResponseEntity<HttpStatus> acceptJoinCommunityRequest(@PathVariable("communityId") UUID communityId, @PathVariable("userId") UUID userId) {
        communityService.addMember(communityId, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{communityId}/cancelJoinCommunityRequest/{userId}")
    public ResponseEntity<HttpStatus> cancelJoinCommunityRequest(@PathVariable("communityId") UUID communityId, @PathVariable("userId") UUID userId) {
        communityService.deleteJoinCommunityRequest(communityId, userId, authenticationFacade.getUserID());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public CommunityDTO convertToCommunityDTO(Community community) {
        return modelMapper.map(community, CommunityDTO.class);
    }

    public CommunityProfileDTO convertToCommunityProfileDTO(Community community) {
        CommunityProfileDTO communityProfileDTO = new CommunityProfileDTO();
        communityProfileDTO.setId(community.getId());
        communityProfileDTO.setName(community.getName());
        communityProfileDTO.setDescription(community.getDescription());
        communityProfileDTO.setCategory(community.getCategory());
        communityProfileDTO.setPhotoLink(community.getPhotoLink());
        communityProfileDTO.setPublishPermission(community.getPublishPermission());
        communityProfileDTO.setJoinType(community.getJoinType());
        communityProfileDTO.setCreatedDate(community.getCreatedDate());
        communityProfileDTO.setPosts(mapPostsToPostDTOs(community.getPosts()));
        communityProfileDTO.setFollowers(mapUsersToUserDTOs(communityService.getFollowers(community.getId())));
        return communityProfileDTO;
    }

    private Set<PostDTO> mapPostsToPostDTOs(Set<Post> posts) {
        return posts.stream().map(post -> modelMapper.map(post, PostDTO.class)).collect(Collectors.toSet());
    }

    private Set<UserDTO> mapUsersToUserDTOs(Set<User> users) {
        return users.stream().map(user -> modelMapper.map(user, UserDTO.class)).collect(Collectors.toSet());
    }
}