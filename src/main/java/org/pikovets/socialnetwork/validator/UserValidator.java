package org.pikovets.socialnetwork.validator;

import org.pikovets.socialnetwork.model.User;
import org.pikovets.socialnetwork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
        User user = (User) target;

        if (!userService.getUserByEmail(user.getEmail()).getResponseList().isEmpty()) {
            errors.rejectValue("email", "This email is already taken");
        }
    }
}
