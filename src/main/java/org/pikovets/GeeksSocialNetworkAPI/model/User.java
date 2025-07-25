package org.pikovets.GeeksSocialNetworkAPI.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.*;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("\"user\"")
public class User {
    @Id
    @Column("id")
    private UUID id;

    @Size(min = 1, max = 40, message = "First name should be between 1 and 40 characters long")
    @Pattern(regexp = "^[A-Za-z-' ]+$", message = "Make sure you don't use numbers or symbols in your first name")
    @Column("first_name")
    private String firstName;

    @Size(max = 50, message = "Last name should be less than 50 characters")
    @Column("last_name")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    @Column("email")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @Column("password")
    private String password;

    @Column("photo_link")
    private String photoLink;

    @NotNull
    @Column("is_active")
    private Boolean isActive;

    @NotNull
    @Column("role")
    private Role role;

    @Override
    public Object clone() throws CloneNotSupportedException {
        User user = new User();
        user.setFirstName(this.getFirstName());
        user.setLastName(this.getLastName());
        user.setEmail(this.getEmail());
        user.setPassword(this.getPassword());
        user.setPhotoLink(this.getPhotoLink());
        user.setIsActive(this.getIsActive());
        user.setRole(this.getRole());
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
}
