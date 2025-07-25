package org.pikovets.GeeksSocialNetworkAPI.service;

import io.jsonwebtoken.lang.Strings;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.ProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.ProfileUpdateRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.UserProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserUpdateRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.ProfileRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ProfileService {
    private static final String USER_NOT_FOUND = "User not found";

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TransactionalOperator transactionalOperator;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, TransactionalOperator transactionalOperator) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<ProfileDTO> getProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).map(this::convertToProfileDTO);
    }

    public Mono<ProfileDTO> saveEmptyProfile(UUID userId) {
        Profile profile = new Profile();
        profile.setUserId(userId);

        return profileRepository.save(profile).as(transactionalOperator::transactional).map(this::convertToProfileDTO);
    }

    public Mono<Void> updateUser(UserProfileDTO userProfileDTO, UUID userID) {
        return userRepository.findById(userID)
                .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)))
                .zipWhen(user -> profileRepository.findByUserId(userID)
                        .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))))
                .flatMap(tuple -> {
                    User currentUser = tuple.getT1();
                    Profile currentProfile = tuple.getT2();
                    applyUserUpdates(currentUser, userProfileDTO.getUserUpdate());
                    applyProfileUpdates(currentProfile, userProfileDTO.getProfileUpdate());

                    return userRepository.save(currentUser)
                            .then(profileRepository.save(currentProfile))
                            .as(transactionalOperator::transactional)
                            .then();
                });
    }

    private void applyUserUpdates(User user, UserUpdateRequest update) {
        if (Strings.hasText(update.getFirstName())) {
            user.setFirstName(update.getFirstName());
        }
        if (Strings.hasText(update.getLastName())) {
            user.setLastName(update.getLastName());
        }
        if (Strings.hasText(update.getEmail())) {
            user.setEmail(update.getEmail());
        }
        if (Strings.hasText(update.getNewPassword())) {
            user.setPassword(passwordEncoder.encode(update.getNewPassword()));
        }
        if (Strings.hasText(update.getPhotoLink())) {
            user.setPhotoLink(update.getPhotoLink());
        }
    }

    private void applyProfileUpdates(Profile profile, ProfileUpdateRequest update) {
        if (Strings.hasText(update.getBio())) {
            profile.setBio(update.getBio());
        }
        if (Strings.hasText(update.getSex())) {
            profile.setSex(update.getSex());
        }
        if (Strings.hasText(update.getAddress())) {
            profile.setAddress(update.getAddress());
        }
        if (update.getBirthday() != null) {
            profile.setBirthday(update.getBirthday());
        }
    }


    private ProfileDTO convertToProfileDTO(Profile profile) {
        return modelMapper.map(profile, ProfileDTO.class);
    }
}