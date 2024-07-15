package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.core.ErrorUtils;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.ProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.UserProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.pikovets.GeeksSocialNetworkAPI.service.ProfileService;
import org.pikovets.GeeksSocialNetworkAPI.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/profiles")
public class ProfileController {
    private final ProfileService profileService;
    private final UserValidator userValidator;
    private final ModelMapper modelMapper;
    private final IAuthenticationFacade authenticationFacade;
    private final PostService postService;

    @Autowired
    ProfileController(ProfileService profileService, UserValidator userValidator, ModelMapper modelMapper, IAuthenticationFacade authenticationFacade, PostService postService) {
        this.profileService = profileService;
        this.userValidator = userValidator;
        this.modelMapper = modelMapper;
        this.authenticationFacade = authenticationFacade;
        this.postService = postService;
    }

    @Operation(
            summary = "Get current user profile",
            description = "Return current authorized user's profile",
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
    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getCurrentUserProfile() {
        return new ResponseEntity<>(convertToProfileDTO(profileService.getProfileByUserId(authenticationFacade.getUserID())), HttpStatus.OK);
    }

    @Operation(
            summary = "Get specific user profile",
            description = "Return specific user's profile by user id",
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
    public ResponseEntity<ProfileDTO> getSpecificProfile(@PathVariable("id") UUID userId) {
        return new ResponseEntity<>(convertToProfileDTO(profileService.getProfileByUserId(userId)), HttpStatus.OK);
    }

    @PatchMapping("/me")
    public ResponseEntity<HttpStatus> updateCurrentUserData(@RequestBody @Valid UserProfileDTO userProfileDTO, BindingResult bindingResult) {
        userValidator.validate(userProfileDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            ErrorUtils.returnBadRequestException(bindingResult);
        }

        profileService.updateUser(userProfileDTO, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Create post",
            description = "Creates a post on the my wall",
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
    @PostMapping("/me/wall")
    public ResponseEntity<HttpStatus> createPost(@RequestBody CreatePostRequest createRequest) {
        postService.createPost(createRequest, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/wall")
    public ResponseEntity<HttpStatus> createCommunityPost(@PathVariable("id") String communityId, @RequestBody CreatePostRequest createRequest) {
        postService.createPost(createRequest, authenticationFacade.getUserID(), UUID.fromString(communityId));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{id}/wall")
    public ResponseEntity<PostResponse> getPosts(@PathVariable("id") String entityId) {
        UUID entityUUID = null;
        if (entityId.equals("me")) {
            entityUUID = authenticationFacade.getUserID();
        } else {
            entityUUID = UUID.fromString(entityId);
        }

        return new ResponseEntity<>(
                new PostResponse(postService.getPosts(entityUUID).stream().map(this::convertToPostDTO).toList()), HttpStatus.OK);
    }

    public PostDTO convertToPostDTO(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }

    public Post convertToPost(PostDTO postDTO) {
        return modelMapper.map(postDTO, Post.class);
    }

    public ProfileDTO convertToProfileDTO(Profile profile) {
        return modelMapper.map(profile, ProfileDTO.class);
    }

    public Profile convertToProfile(ProfileDTO profileDTO) {
        return modelMapper.map(profileDTO, Profile.class);
    }
}