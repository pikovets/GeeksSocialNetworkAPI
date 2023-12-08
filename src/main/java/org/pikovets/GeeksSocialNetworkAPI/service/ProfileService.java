package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserService userService;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, UserService userService) {
        this.profileRepository = profileRepository;
        this.userService = userService;
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
    public void edit(User updatedUser, Profile updatedProfile, UUID userId) {
        User userToBeUpdated = userService.getUserById(userId);
        Profile profileToBeUpdated = getProfileByUserId(userId);

        userService.mergeUsers(userToBeUpdated, updatedUser);
        mergeProfiles(profileToBeUpdated, updatedProfile);
    }

    public void mergeProfiles(Profile profileToBeUpdated, Profile newProfile) {
        profileToBeUpdated.setBio((newProfile.getBio() != null && !newProfile.getBio().isEmpty())
                ? newProfile.getBio()
                : profileToBeUpdated.getBio());

        profileToBeUpdated.setBio((newProfile.getSex() != null && !newProfile.getSex().isEmpty())
                ? newProfile.getSex()
                : profileToBeUpdated.getSex());

        profileToBeUpdated.setBio((newProfile.getAddress() != null && !newProfile.getAddress().isEmpty())
                ? newProfile.getAddress()
                : profileToBeUpdated.getAddress());

        profileToBeUpdated.setBirthday((newProfile.getBirthday() != null && !newProfile.getBirthday().toString().isEmpty())
                ? newProfile.getBirthday()
                : profileToBeUpdated.getBirthday());
    }
}
