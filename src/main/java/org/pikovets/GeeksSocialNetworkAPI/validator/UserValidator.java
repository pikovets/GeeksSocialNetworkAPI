package org.pikovets.GeeksSocialNetworkAPI.validator;

import org.pikovets.GeeksSocialNetworkAPI.dto.profile.UserProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserUpdateDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@Component
public class UserValidator implements Validator {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final IAuthenticationFacade authentication;

    @Autowired
    public UserValidator(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, IAuthenticationFacade authentication) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authentication = authentication;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz) || SignUpDTO.class.equals(clazz) || UserProfileDTO.class.equals(clazz) || UserUpdateDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof SignUpDTO signUpDTO) {
            validateEmail(signUpDTO.getEmail(), errors);
        } else if (target instanceof UserProfileDTO userProfileDTO) {
            System.out.println(userProfileDTO);
            UserUpdateDTO user = userProfileDTO.getUserUpdate();
            validateEmail(user.getEmail(), authentication.getUserID(), errors);
            if (user.getOldPassword() != null && !user.getOldPassword().isEmpty()) {
                validatePassword(user.getOldPassword(), errors);
            }
        } else if (target instanceof User user) {
            validateEmail(user.getEmail(), errors);
        }
    }

    private void validateEmail(String email, Errors errors) {
        try {
            if (userService.getUserByEmail(email) != null) {
                errors.rejectValue("email", "This email is already taken");
            }
        } catch (NotFoundException ignored) {
        }
    }

    private void validateEmail(String email, UUID userId, Errors errors) {
        Mono<User> existingUser = userService.getUserByEmail(email);
        existingUser.subscribe(user -> {
            if (user != null && !Objects.equals(user.getId(), userId)) {
                errors.rejectValue("user.email", "This email is already taken");
            }
        });
    }

    private void validatePassword(String password, Errors errors) {
        userRepository.findById(authentication.getUserID()).subscribe(user -> {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                errors.rejectValue("user.oldPassword", "Password mismatch. Double-check your entry");
            }
        });
    }
}