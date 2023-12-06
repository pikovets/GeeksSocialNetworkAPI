package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProfileService {
    public final ProfileRepository profileRepository;
    public final UserService userService;

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
    public void edit(Profile profile, UUID userId) {
        profile.setId(userId);
    }
}
