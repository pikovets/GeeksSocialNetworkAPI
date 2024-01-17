package org.pikovets.GeeksSocialNetworkAPI.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserUpdateDTO;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private UserUpdateDTO user;
    private Profile profile;
}
