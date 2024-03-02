package org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRelationshipDTO {
    @NotNull
    private UserDTO requester;

    @NotNull
    private UserDTO acceptor;

    @NotNull
    private RelationshipType type;

    @Override
    public int hashCode() {
        return requester.hashCode() + acceptor.hashCode();
    }
}
