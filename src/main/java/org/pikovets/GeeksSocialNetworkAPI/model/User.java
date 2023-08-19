package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "\"user\"")
public class User {
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

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive;

    @NotNull
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}