package org.pikovets.GeeksSocialNetworkAPI.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserUpdateDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private UserUpdateDTO userUpdate;
    private ProfileDTO profile;
}
