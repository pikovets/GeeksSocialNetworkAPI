package org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRelationshipDTO {

    @NotNull
    private UUID requesterId;

    @NotNull
    private UUID acceptorId;

    @NotNull
    private RelationshipType type;
}
