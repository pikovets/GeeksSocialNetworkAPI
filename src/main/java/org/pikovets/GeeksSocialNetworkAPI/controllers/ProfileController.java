package org.pikovets.GeeksSocialNetworkAPI.controllers;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.ProfileDTO;
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

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getCurrentUserProfile() {
        return new ResponseEntity<>(convertToProfileDTO(profileService.getProfileByUserId(authenticationFacade.getUserID())), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> getSpecificProfile(@PathVariable("id") UUID userId) {
        return new ResponseEntity<>(convertToProfileDTO(profileService.getProfileByUserId(userId)), HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<HttpStatus> saveEmptyProfile(@PathVariable("id") UUID userId) {
        profileService.saveEmptyProfile(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/me")
    public ResponseEntity<HttpStatus> editCurrentUserProfile(@RequestBody ProfileDTO profileDTO) {
        profileService.edit(convertToProfile(profileDTO), authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ProfileDTO convertToProfileDTO(Profile profile) {
        return modelMapper.map(profile, ProfileDTO.class);
    }

    public Profile convertToProfile(ProfileDTO profileDTO) {
        return modelMapper.map(profileDTO, Profile.class);
    }
}
