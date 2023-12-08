package org.pikovets.GeeksSocialNetworkAPI.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.model.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private User user;
    private Profile profile;
}
