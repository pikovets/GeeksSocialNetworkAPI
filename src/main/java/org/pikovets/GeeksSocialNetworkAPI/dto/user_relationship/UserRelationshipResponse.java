package org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRelationshipResponse {
    private Flux<UserRelationshipDTO> friends;
}
