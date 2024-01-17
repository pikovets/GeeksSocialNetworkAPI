package org.pikovets.GeeksSocialNetworkAPI.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "profile")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "bio")
    @Size(max = 150, message = "Bio should be less than 150 characters long")
    private String bio;

    @Column(name = "birthday")
    @JsonFormat(pattern="MM/dd/yyyy")
    private Date birthday;

    @Column(name = "sex")
    @Size(max = 25, message = "Sex should be less than 25 characters long")
    private String sex;

    @Column(name = "address")
    @Size(max = 255, message = "Address should be less than 255 characters long")
    private String address;

    @NotNull
    @Column(name = "join_date")
    private Date joinDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", bio='" + bio + '\'' +
                ", birthday=" + birthday +
                ", sex='" + sex + '\'' +
                ", address='" + address + '\'' +
                ", joinDate=" + joinDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile profile)) return false;

        return user.equals(profile.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }
}
