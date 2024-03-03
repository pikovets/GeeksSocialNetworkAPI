package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CommunityResponse {
    private List<CommunityDTO> communities;
}
