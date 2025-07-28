package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class ProfileServiceTest {
    private ProfileRepository profileRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ModelMapper modelMapper;
    private TransactionalOperator transactionalOperator;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        modelMapper = mock(ModelMapper.class);
        transactionalOperator = mock(TransactionalOperator.class);

        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        profileService = new ProfileService(
                profileRepository,
                userRepository,
                passwordEncoder,
                modelMapper,
                transactionalOperator
        );
    }

    @Test
    void getProfileByUserId_found() {
        UUID userId = UUID.randomUUID();
        Profile profile = new Profile();
        ProfileDTO dto = new ProfileDTO();

        when(profileRepository.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(modelMapper.map(profile, ProfileDTO.class)).thenReturn(dto);

        StepVerifier.create(profileService.getProfileByUserId(userId))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void getProfileByUserId_notFound() {
        UUID userId = UUID.randomUUID();

        when(profileRepository.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(profileService.getProfileByUserId(userId))
                .expectErrorMatches(e -> e instanceof NotFoundException && e.getMessage().equals("User not found"))
                .verify();
    }

    @Test
    void saveEmptyProfile_success() {
        UUID userId = UUID.randomUUID();
        Profile savedProfile = new Profile();
        savedProfile.setUserId(userId);
        ProfileDTO dto = new ProfileDTO();

        when(profileRepository.save(any(Profile.class))).thenReturn(Mono.just(savedProfile));
        when(modelMapper.map(savedProfile, ProfileDTO.class)).thenReturn(dto);

        StepVerifier.create(profileService.saveEmptyProfile(userId))
                .expectNext(dto)
                .verifyComplete();

        verify(profileRepository).save(argThat(profile -> profile.getUserId().equals(userId)));
    }

    @Test
    void updateUser_success() {
        UUID userId = UUID.randomUUID();

        User existingUser = new User();
        existingUser.setFirstName("OldFirst");
        existingUser.setLastName("OldLast");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword("oldPass");
        existingUser.setPhotoLink("oldPhoto");

        Profile existingProfile = new Profile();
        existingProfile.setBio("Old bio");
        existingProfile.setSex("Old sex");
        existingProfile.setAddress("Old address");
        existingProfile.setBirthday(LocalDate.of(2000, 1, 1));

        UserUpdateRequest userUpdate = new UserUpdateRequest();
        userUpdate.setFirstName("NewFirst");
        userUpdate.setLastName("NewLast");
        userUpdate.setEmail("new@example.com");
        userUpdate.setNewPassword("newPass");
        userUpdate.setPhotoLink("newPhoto");

        ProfileUpdateRequest profileUpdate = new ProfileUpdateRequest();
        profileUpdate.setBio("New bio");
        profileUpdate.setSex("New sex");
        profileUpdate.setAddress("New address");
        profileUpdate.setBirthday(LocalDate.of(1990, 12, 31));

        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setUserUpdate(userUpdate);
        userProfileDTO.setProfileUpdate(profileUpdate);

        when(userRepository.findById(userId)).thenReturn(Mono.just(existingUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Mono.just(existingProfile));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(userRepository.save(existingUser)).thenReturn(Mono.just(existingUser));
        when(profileRepository.save(existingProfile)).thenReturn(Mono.just(existingProfile));

        StepVerifier.create(profileService.updateUser(userProfileDTO, userId))
                .verifyComplete();

        assert existingUser.getFirstName().equals("NewFirst");
        assert existingUser.getLastName().equals("NewLast");
        assert existingUser.getEmail().equals("new@example.com");
        assert existingUser.getPassword().equals("encodedNewPass");
        assert existingUser.getPhotoLink().equals("newPhoto");

        assert existingProfile.getBio().equals("New bio");
        assert existingProfile.getSex().equals("New sex");
        assert existingProfile.getAddress().equals("New address");
        assert existingProfile.getBirthday().equals(LocalDate.of(1990, 12, 31));
    }

    @Test
    void updateUser_userNotFound() {
        UUID userId = UUID.randomUUID();
        UserProfileDTO dto = new UserProfileDTO();

        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(profileService.updateUser(dto, userId))
                .expectErrorMatches(e -> e instanceof NotFoundException && e.getMessage().equals("User not found"))
                .verify();
    }

    @Test
    void updateUser_profileNotFound() {
        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        UserProfileDTO dto = new UserProfileDTO();

        when(userRepository.findById(userId)).thenReturn(Mono.just(existingUser));
        when(profileRepository.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(profileService.updateUser(dto, userId))
                .expectErrorMatches(e -> e instanceof NotFoundException && e.getMessage().equals("User not found"))
                .verify();
    }
}
