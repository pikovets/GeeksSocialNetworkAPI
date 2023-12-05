package org.pikovets.GeeksSocialNetworkAPI.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpDTO {
    @NotEmpty(message = "Full name cannot be empty")
    @Size(min = 1, max = 70, message = "Full name should be between 1 and 70 characters long")
    @Pattern(regexp = "^[A-Za-z-' ]+$", message = "Enter only letters (A-Z, a-z), spaces, apostrophes")
    private String fullName;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    private String password;
}
