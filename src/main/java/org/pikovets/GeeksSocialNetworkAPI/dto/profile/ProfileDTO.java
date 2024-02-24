package org.pikovets.GeeksSocialNetworkAPI.dto.profile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {
    @Size(max = 150, message = "Bio should be less than 150 characters long")
    private String bio;

    private Date birthday;

    @Size(max = 25, message = "Sex should be less than 25 characters long")
    private String sex;

    @Size(max = 255, message = "Address should be less than 255 characters long")
    private String address;

    @NotNull
    private Date joinDate;
}