package org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserRelationshipResponse {
    private List<UserRelationshipDTO> friends;
}
