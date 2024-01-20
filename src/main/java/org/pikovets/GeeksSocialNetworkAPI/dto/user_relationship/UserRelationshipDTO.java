package org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.model.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.model.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRelationshipDTO {
    @NotNull
    private User requester;

    @NotNull
    private User acceptor;

    @NotNull
    private RelationshipType relationshipType;
}
