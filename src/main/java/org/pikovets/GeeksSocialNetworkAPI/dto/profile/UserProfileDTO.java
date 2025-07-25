package org.pikovets.GeeksSocialNetworkAPI.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserUpdateRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private UserUpdateRequest userUpdate;
    private ProfileUpdateRequest profileUpdate;
}
