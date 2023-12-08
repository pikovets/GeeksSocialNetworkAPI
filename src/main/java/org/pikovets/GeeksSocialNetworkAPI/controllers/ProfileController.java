package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.ProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.UserProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/profiles")
public class ProfileController {
    private final ProfileService profileService;
    private final ModelMapper modelMapper;
    private final IAuthenticationFacade authenticationFacade;

    @Autowired
    ProfileController(ProfileService profileService, ModelMapper modelMapper, IAuthenticationFacade authenticationFacade) {
        this.profileService = profileService;
        this.modelMapper = modelMapper;
        this.authenticationFacade = authenticationFacade;
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
    public ResponseEntity<HttpStatus> editCurrentUserProfile(@RequestBody UserProfileDTO userProfileDTO) {
        profileService.update(userProfileDTO.getUser(), userProfileDTO.getProfile(), authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ProfileDTO convertToProfileDTO(Profile profile) {
        return modelMapper.map(profile, ProfileDTO.class);
    }

    public Profile convertToProfile(ProfileDTO profileDTO) {
        return modelMapper.map(profileDTO, Profile.class);
    }
}
