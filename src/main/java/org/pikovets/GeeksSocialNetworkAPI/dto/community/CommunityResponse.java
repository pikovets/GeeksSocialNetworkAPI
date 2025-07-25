package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class CommunityResponse {
    private Flux<CommunityDTO> communities;
}
