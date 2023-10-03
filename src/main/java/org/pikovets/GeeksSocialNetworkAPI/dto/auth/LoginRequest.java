package org.pikovets.GeeksSocialNetworkAPI.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequest {
    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    private String password;
}