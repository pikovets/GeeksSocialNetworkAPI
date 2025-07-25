package org.pikovets.GeeksSocialNetworkAPI.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("profile")
public class Profile {
    @Id
    @Column("id")
    private UUID id;

    @Column("bio")
    @Size(max = 150, message = "Bio should be less than 150 characters long")
    private String bio;

    @Column("birthday")
    private LocalDate birthday;

    @Column("sex")
    @Size(max = 25, message = "Sex should be less than 25 characters long")
    private String sex;

    @Column("address")
    @Size(max = 255, message = "Address should be less than 255 characters long")
    private String address;

    @Column("join_date")
    private LocalDate joinDate;

    @NotNull
    @Column("user_id")
    private UUID userId;
}
