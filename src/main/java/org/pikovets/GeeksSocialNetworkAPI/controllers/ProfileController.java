package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.ProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.UserProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.pikovets.GeeksSocialNetworkAPI.service.ProfileService;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/profiles")
@Tag(name = "Profile")
public class ProfileController {
    private final ProfileService profileService;
    private final UserService userService;
    private final IAuthenticationFacade authenticationFacade;
    private final PostService postService;

    @Autowired
    ProfileController(ProfileService profileService, UserService userService, IAuthenticationFacade authenticationFacade, PostService postService) {
        this.profileService = profileService;
        this.userService = userService;
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
                            responseCode = "401",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/me")
    public Mono<ProfileDTO> getCurrentUserProfile() {
        return authenticationFacade.getUserID().flatMap(profileService::getProfileByUserId);
    }

    @Operation(
            summary = "Get user profile",
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
                            responseCode = "401",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/{id}")
    public Mono<ProfileDTO> getSpecificProfile(@PathVariable("id") UUID userId) {
        return profileService.getProfileByUserId(userId);
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
                            responseCode = "401",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @PatchMapping("/me")
    public Mono<Void> updateCurrentUserData(@RequestBody @Valid Mono<UserProfileDTO> userProfileDTOMono) {
        return authenticationFacade
                .getUserID()
                .flatMap(authUserId ->
                        userProfileDTOMono.flatMap(userProfileDTO -> {
                            Mono<Void> emailValidation = Mono.empty();
                            if (userProfileDTO.getUserUpdate().getEmail() != null) {
                                emailValidation = userService.validateEmailUnique(userProfileDTO.getUserUpdate().getEmail());
                            }
                            Mono<Void> passwordValidation = Mono.empty();
                            if (userProfileDTO.getUserUpdate().getOldPassword() != null) {
                                passwordValidation = userService.validatePassword(authUserId, userProfileDTO.getUserUpdate().getOldPassword());
                            }
                            return emailValidation
                                    .then(passwordValidation)
                                    .then(profileService.updateUser(userProfileDTO, authUserId));
                        })
                );
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
                            responseCode = "401",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @PostMapping("/me/wall")
    public Mono<Void> createPost(@RequestBody Mono<CreatePostRequest> createRequest) {
        return authenticationFacade.getUserID().flatMap(authUserId -> postService.createPost(createRequest, authUserId)).then();
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
                            responseCode = "401",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @PostMapping("/{id}/wall")
    public Mono<Void> createCommunityPost(@PathVariable("id") String communityId, @RequestBody Mono<CreatePostRequest> createRequest) {
        return authenticationFacade.getUserID().flatMap(authUserId -> postService.createPost(createRequest, authUserId, UUID.fromString(communityId))).then();
    }

    @Operation(
            summary = "Get all the posts on the wall",
            description = "Get all posts on the wall by user or community id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Entity id",
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
                            responseCode = "401",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/{id}/wall")
    public Flux<PostDTO> getPosts(@PathVariable("id") String entityId) {
        if (entityId.equals("me")) {
            return authenticationFacade.getUserID().flatMapMany(postService::getPosts);
        } else {
            return postService.getPosts(UUID.fromString(entityId));
        }
    }
}