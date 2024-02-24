package org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRelationshipRequest {
    private UUID userId;
}
