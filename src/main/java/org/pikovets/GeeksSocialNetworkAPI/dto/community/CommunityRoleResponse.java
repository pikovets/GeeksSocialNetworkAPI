package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import reactor.core.publisher.Mono;

@Data
@AllArgsConstructor
public class CommunityRoleResponse {
    private Mono<CommunityRole> role;
}
