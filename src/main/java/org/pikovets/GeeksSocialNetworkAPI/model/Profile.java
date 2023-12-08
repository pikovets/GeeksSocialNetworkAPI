package org.pikovets.GeeksSocialNetworkAPI.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Profile")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "bio")
    @Max(value = 150, message = "Bio should be less than 150 characters long")
    private String bio;

    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "sex")
    @Max(value = 25, message = "Sex should be less than 25 characters long")
    private String sex;

    @Column(name = "address")
    @Max(value = 255, message = "Address should be less than 255 characters long")
    private String address;

    @NotNull
    @Column(name = "join_date")
    private Date joinDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
