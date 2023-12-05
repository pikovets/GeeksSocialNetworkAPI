package org.pikovets.GeeksSocialNetworkAPI.validator;

import org.pikovets.GeeksSocialNetworkAPI.dto.user.SignUpDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    private final UserService userService;

    @Autowired
    public UserValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof SignUpDTO) {
            validateSignUpDTO((SignUpDTO) target, errors);
        } else if (target instanceof User) {
            validateUser((User) target, errors);
        }
    }

    private void validateSignUpDTO(SignUpDTO user, Errors errors) {
        try {
            if (userService.getUserByEmail(user.getEmail()) != null) {
                errors.rejectValue("email", "This email is already taken");
            }
        } catch (NotFoundException ignored) {
        }
    }

    private void validateUser(User user, Errors errors) {
        try {
            if (userService.getUserByEmail(user.getEmail()) != null) {
                errors.rejectValue("email", "This email is already taken");
            }
        } catch (NotFoundException ignored) {
        }
    }
}
