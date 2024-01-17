package org.pikovets.GeeksSocialNetworkAPI.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Model class representing a user update request in the system.
 * This class extends the standard User model, incorporating additional fields
 * to handle password updates specifically. The update request is distinct from
 * the regular user model and includes two new fields: oldPassword and newPassword.
 * These fields are utilized when updating the user's password, providing the
 * necessary information to authenticate the user and set the new password securely.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    @Size(min = 1, max = 40, message = "First name should be between 1 and 40 characters long")
    @Pattern(regexp = "^[A-Za-z-' ]+$", message = "Make sure you don't use numbers or symbols in your first name")
    private String firstName;

    @Size(max = 50, message = "Last name should be less than 50 characters")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    private String photoLink;

    /**
     * The old password is required for authentication purposes when updating the user's password.
     */
    private String oldPassword;

    /**
     * The new password represents the desired password to be set during the update process.
     */
    private String newPassword;
}
