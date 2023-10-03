package org.pikovets.GeeksSocialNetworkAPI.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;

    @NotEmpty(message = "First name cannot be empty")
    @Size(min = 2, max = 30, message = "First name should be between 2 and 30 characters long")
    @Pattern(regexp = "^[A-Za-z\\\\p{L}]+$", message = "Make sure you don't use numbers or symbols in your first name")
    private String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    @Size(min = 2, max = 30, message = "Last name should be between 2 and 30 characters long")
    @Pattern(regexp = "^[A-Za-z\\\\p{L}]+$", message = "Make sure you don't use numbers or symbols in your last name")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    private String password;
}