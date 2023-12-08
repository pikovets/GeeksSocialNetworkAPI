package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.user.AuthDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Role;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager, ProfileService profileService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @Transactional
    public User registerUser(SignUpDTO registeredUser) {
        User user = new User();
        String[] names = registeredUser.getFullName().split(" ", 2);

        user.setFirstName(names[0]);
        user.setLastName(names.length == 2 ? names[1] : "");
        user.setEmail(registeredUser.getEmail());
        user.setPassword(passwordEncoder.encode(registeredUser.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(true);

        userRepository.save(user);

        profileService.saveEmptyProfile(user.getId());

        return user;
    }

    @Transactional
    public TokenResponse loginUser(AuthDTO authDTO) {
        try {
            User user = userRepository.findByEmail(authDTO.getEmail())
                    .orElseThrow(() -> new NotFoundException("User not found"));

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getId(),
                            authDTO.getPassword()
                    )
            );

            String jwtToken = jwtUtils.generateToken(user);

            return new TokenResponse(jwtToken);

        } catch (Exception e) {
            throw new BadRequestException("Incorrect username or password");
        }
    }
}
