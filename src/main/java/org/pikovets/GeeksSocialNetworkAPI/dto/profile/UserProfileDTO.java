package org.pikovets.GeeksSocialNetworkAPI.dto.profile;

import lombok.Data;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.model.User;

@Data
public class UserProfileDTO {
    private User user;
    private Profile profile;
}
