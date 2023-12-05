package org.pikovets.GeeksSocialNetworkAPI.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PostResponse {
    private List<PostDTO> posts;
}