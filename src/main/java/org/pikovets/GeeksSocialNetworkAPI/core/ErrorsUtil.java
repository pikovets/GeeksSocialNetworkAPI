package org.pikovets.GeeksSocialNetworkAPI.core;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public class ErrorsUtil {
    public static <T> CustomResponse<T> returnBadRequestException(T object, BindingResult bindingResult) {
        StringBuilder stringBuilder = new StringBuilder();

        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            stringBuilder.append(error.getField())
                    .append(": ").append(error.getDefaultMessage() == null ? error.getCode() : error.getDefaultMessage())
                    .append("; ");
        }

        CustomResponse<T> customResponse = new CustomResponse<>(List.of(object), CustomStatus.BAD_REQUEST);
        customResponse.setMessage(stringBuilder.toString().trim());

        return customResponse;
    }
}