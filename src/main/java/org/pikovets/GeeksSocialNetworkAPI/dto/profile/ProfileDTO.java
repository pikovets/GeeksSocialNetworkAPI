package org.pikovets.GeeksSocialNetworkAPI.dto.profile;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.util.Date;

@Data
public class ProfileDTO {
    @Column(name = "bio")
    @Max(value = 150, message = "Bio should be less than 150 characters long")
    private String bio;

    private Date birthday;

    @Max(value = 25, message = "Sex should be less than 25 characters long")
    private String sex;

    @Max(value = 255, message = "Address should be less than 255 characters long")
    private String address;

    private Date join_date;
}
