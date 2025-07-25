package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCommunityDTO {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID communityId;

    @NotNull
    private CommunityRole userRole;
}
