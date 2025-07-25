package org.pikovets.GeeksSocialNetworkAPI.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLikeDTO {

    @NotNull
    private UUID id;

    @NotNull
    private UUID postId;

    @NotNull
    private UUID userId;
}
