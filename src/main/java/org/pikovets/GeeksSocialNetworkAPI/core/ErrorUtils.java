package org.pikovets.GeeksSocialNetworkAPI.core;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.UserBadRequestException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public class ErrorUtils {
    public static void returnBadRequestException(BindingResult bindingResult) {
        StringBuilder stringBuilder = new StringBuilder();

        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            stringBuilder.append(error.getField())
                    .append(": ").append(error.getDefaultMessage() == null ? error.getCode() : error.getDefaultMessage())
                    .append("; ");
        }

        throw new UserBadRequestException(stringBuilder.toString().trim());
    }
}