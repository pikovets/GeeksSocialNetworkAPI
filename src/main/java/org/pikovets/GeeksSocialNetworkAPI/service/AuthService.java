package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.TokenResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.AuthDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final TransactionalOperator transactionalOperator;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, ProfileService profileService, TransactionalOperator transactionalOperator) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<Void> registerUser(Mono<SignUpRequest> signUpDTOMono) {
        return signUpDTOMono
                .flatMap(signUpRequest -> Mono.fromCallable(() -> {
                    String[] names = signUpRequest.getFullName().split(" ", 2);
                    User user = new User();
                    user.setFirstName(names[0]);
                    user.setLastName(names.length == 2 ? names[1] : "");
                    user.setEmail(signUpRequest.getEmail());
                    user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
                    user.setRole(Role.USER);
                    user.setIsActive(true);
                    return user;
                }).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(userRepository::save)
                .flatMap(savedUser -> profileService.saveEmptyProfile(savedUser.getId()))
                .as(this.transactionalOperator::transactional)
                .then();
    }

    public Mono<TokenResponse> loginUser(Mono<AuthDTO> authDTOMono) {
        return authDTOMono.flatMap(authDTO -> userRepository.findByEmail(authDTO.getEmail()).switchIfEmpty(Mono.error(new BadRequestException("Incorrect username or password"))).flatMap(user -> {
            if (!passwordEncoder.matches(authDTO.getPassword(), user.getPassword())) {
                return Mono.error(new BadRequestException("Incorrect username or password"));
            }
            return jwtUtils.generateToken(Mono.just(user))
                    .map(TokenResponse::new);
        }));
    }
}