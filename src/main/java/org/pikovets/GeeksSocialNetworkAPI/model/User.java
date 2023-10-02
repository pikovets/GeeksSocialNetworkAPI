package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "\"user\"")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotEmpty(message = "First name cannot be empty")
    @Size(min = 2, max = 30, message = "First name should be between 2 and 30 characters long")
    @Pattern(regexp = "^[A-Za-z\\\\p{L}]+$", message = "Make sure you don't use numbers or symbols in your first name")
    @Column(name = "first_name")
    private String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    @Size(min = 2, max = 30, message = "Last name should be between 2 and 30 characters long")
    @Pattern(regexp = "^[A-Za-z\\\\p{L}]+$", message = "Make sure you don't use numbers or symbols in your last name")
    @Column(name = "last_name")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    @Column(name = "email")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @Column(name = "password")
    private String password;

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive;

    @NotNull
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @OneToMany(mappedBy = "author")
    private Set<Post> posts;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}