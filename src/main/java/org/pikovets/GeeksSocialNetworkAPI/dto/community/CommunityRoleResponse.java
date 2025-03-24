package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;

@Data
@AllArgsConstructor
public class CommunityRoleResponse {
    private CommunityRole role;
}
