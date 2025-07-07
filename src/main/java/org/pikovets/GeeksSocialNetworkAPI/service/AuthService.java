package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.user.AuthDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, ProfileService profileService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    public Mono<Void> registerUser(SignUpDTO registeredUser) {
        String[] names = registeredUser.getFullName().split(" ", 2);

        User user = new User();
        user.setFirstName(names[0]);
        user.setLastName(names.length == 2 ? names[1] : "");
        user.setEmail(registeredUser.getEmail());
        user.setPassword(passwordEncoder.encode(registeredUser.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(true);

        return userRepository.save(user)
                .flatMap(savedUser -> profileService.saveEmptyProfile(savedUser.getId()))
                .then();
    }

    public Mono<TokenResponse> loginUser(AuthDTO authDTO) {
        return userRepository.findByEmail(authDTO.getEmail())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(authDTO.getPassword(), user.getPassword())) {
                        return Mono.error(new BadRequestException("Incorrect username or password"));
                    }
                    String jwtToken = jwtUtils.generateToken(user);
                    return Mono.just(new TokenResponse(jwtToken));
                });
    }
}