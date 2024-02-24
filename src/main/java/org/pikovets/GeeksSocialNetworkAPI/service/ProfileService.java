package org.pikovets.GeeksSocialNetworkAPI.service;

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

import java.util.Date;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Profile getProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId).orElseThrow(new NotFoundException("User not found"));
    }

    @Transactional
    public void saveEmptyProfile(UUID userId) {
        Profile profile = new Profile();
        profile.setUser(userService.getUserById(userId));
        profile.setJoinDate(new Date());

        profileRepository.save(profile);
    }

    @Transactional
    public void updateUser(UserProfileDTO userProfileDTO, UUID userID) {
        User currentUser = userService.getUserById(userID);
        Profile currentProfile = getProfileByUserId(userID);

        updateUser(currentUser, currentProfile, userProfileDTO);
    }

    @Transactional
    public void updateUser(User currentUser, Profile currentProfile, UserProfileDTO userProfileDTO) {
        UserUpdateDTO updatedUser = userProfileDTO.getUser();
        Profile updatedProfile = userProfileDTO.getProfile();

        updateUserFields(currentUser, updatedUser);
        mergeProfiles(currentProfile, updatedProfile);

        userRepository.save(currentUser);
        profileRepository.save(updatedProfile);
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
        profileToBeUpdated.setBio((newProfile.getBio() != null && !newProfile.getBio().isEmpty())
                ? newProfile.getBio()
                : profileToBeUpdated.getBio());

        profileToBeUpdated.setSex((newProfile.getSex() != null && !newProfile.getSex().isEmpty())
                ? newProfile.getSex()
                : profileToBeUpdated.getSex());

        profileToBeUpdated.setAddress((newProfile.getAddress() != null && !newProfile.getAddress().isEmpty())
                ? newProfile.getAddress()
                : profileToBeUpdated.getAddress());

        profileToBeUpdated.setBirthday((newProfile.getBirthday() != null && !newProfile.getBirthday().toString().isEmpty())
                ? newProfile.getBirthday()
                : profileToBeUpdated.getBirthday());
    }
}
