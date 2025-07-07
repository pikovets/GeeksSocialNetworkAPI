package org.pikovets.GeeksSocialNetworkAPI.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentLikeDTO {

    @NotNull
    private UUID id;

    @NotNull
    private UUID commentId;

    @NotNull
    private UUID userId;
}
