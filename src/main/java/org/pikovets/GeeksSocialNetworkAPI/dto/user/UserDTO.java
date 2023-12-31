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
    @Size(min = 1, max = 40, message = "First name should be between 1 and 40 characters long")
    @Pattern(regexp = "^[A-Za-z-' ]+$", message = "Make sure you don't use numbers or symbols in your first name")
    private String firstName;

    @Size(max = 50, message = "Last name should be less than 50 characters")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;
}