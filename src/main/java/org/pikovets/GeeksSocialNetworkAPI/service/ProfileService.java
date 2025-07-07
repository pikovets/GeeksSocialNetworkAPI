package org.pikovets.GeeksSocialNetworkAPI.service;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.ProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.UserProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserUpdateDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.ProfileRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProfileService {
    private static final String USER_NOT_FOUND = "User not found";

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public Mono<ProfileDTO> getProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).map(this::convertToProfileDTO);
    }

    public Mono<ProfileDTO> saveEmptyProfile(UUID userId) {
        Profile profile = new Profile();
        profile.setUserId(userId);

        return profileRepository.save(profile).map(this::convertToProfileDTO);
    }

    public Mono<Void> updateUser(UserProfileDTO userProfileDTO, UUID userID) {
        return userRepository.findById(userID)
                .flatMap(currentUser -> profileRepository.findByUserId(userID)
                        .flatMap(currentProfile -> {
                            updateUser(currentUser, currentProfile, userProfileDTO);
                            return userRepository.save(currentUser)
                                    .then(profileRepository.save(currentProfile))
                                    .then();
                        }))
                .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)));
    }

    public void updateUser(User currentUser, Profile currentProfile, UserProfileDTO userProfileDTO) {
        UserUpdateDTO updatedUser = userProfileDTO.getUserUpdate();
        Profile updatedProfile = convertToProfile(userProfileDTO.getProfile());

        updateUserFields(currentUser, updatedUser);
        mergeProfiles(currentProfile, updatedProfile);
    }

    public void updateUserFields(User userToBeUpdated, UserUpdateDTO updatedUser) {
        if (updatedUser.getFirstName() != null) {
            userToBeUpdated.setFirstName(updatedUser.getFirstName());
        }

        if (updatedUser.getLastName() != null) {
            userToBeUpdated.setLastName(updatedUser.getLastName());
        }

        if (updatedUser.getEmail() != null) {
            userToBeUpdated.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getNewPassword() != null) {
            userToBeUpdated.setPassword(passwordEncoder.encode(updatedUser.getNewPassword()));
        }

        if (updatedUser.getPhotoLink() != null) {
            userToBeUpdated.setPhotoLink(updatedUser.getPhotoLink());
        }
    }

    public void mergeProfiles(Profile profileToBeUpdated, Profile newProfile) {
        if (newProfile.getBio() != null && !newProfile.getBio().isEmpty()) {
            profileToBeUpdated.setBio(newProfile.getBio());
        }

        if (newProfile.getSex() != null && !newProfile.getSex().isEmpty()) {
            profileToBeUpdated.setSex(newProfile.getSex());
        }

        if (newProfile.getAddress() != null && !newProfile.getAddress().isEmpty()) {
            profileToBeUpdated.setAddress(newProfile.getAddress());
        }

        if (newProfile.getBirthday() != null) {
            profileToBeUpdated.setBirthday(newProfile.getBirthday());
        }
    }

    public ProfileDTO convertToProfileDTO(Profile profile) {
        return modelMapper.map(profile, ProfileDTO.class);
    }

    public Profile convertToProfile(ProfileDTO profileDTO) {
        return modelMapper.map(profileDTO, Profile.class);
    }
}
