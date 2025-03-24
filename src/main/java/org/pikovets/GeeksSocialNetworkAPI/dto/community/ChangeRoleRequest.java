package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import lombok.Data;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;

import java.util.UUID;

@Data
public class ChangeRoleRequest {
    private UUID communityId;
    private CommunityRole newRole;
}
