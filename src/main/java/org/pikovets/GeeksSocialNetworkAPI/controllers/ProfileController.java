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
@Tag(name = "Profile")
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
            description = "Get current authorized user's profile",
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
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getCurrentUserProfile() {
        return new ResponseEntity<>(convertToProfileDTO(profileService.getProfileByUserId(authenticationFacade.getUserID())), HttpStatus.OK);
    }

    @Operation(
            summary = "Get specific user profile",
            description = "Get specific user's profile by user id",
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
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> getSpecificProfile(@PathVariable("id") UUID userId) {
        return new ResponseEntity<>(convertToProfileDTO(profileService.getProfileByUserId(userId)), HttpStatus.OK);
    }

    @Operation(
            summary = "Update current user profile",
            description = "This endpoint allows an authenticated user to update their profile information",
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
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
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
            description = "Creates a post on the current user's wall",
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
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @PostMapping("/me/wall")
    public ResponseEntity<HttpStatus> createPost(@RequestBody CreatePostRequest createRequest) {
        postService.createPost(createRequest, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Create community post",
            description = "Creates a post on the community wall",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Community id",
                            required = true
                    ),
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
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @PostMapping("/{id}/wall")
    public ResponseEntity<HttpStatus> createCommunityPost(@PathVariable("id") String communityId, @RequestBody CreatePostRequest createRequest) {
        postService.createPost(createRequest, authenticationFacade.getUserID(), UUID.fromString(communityId));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Get all the posts on the wall",
            description = "Get all posts on the wall by user or community id",
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
                            description = "Bad Request",
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
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

    public ProfileDTO convertToProfileDTO(Profile profile) {
        return modelMapper.map(profile, ProfileDTO.class);
    }
}