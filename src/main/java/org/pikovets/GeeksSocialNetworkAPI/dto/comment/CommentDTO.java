package org.pikovets.GeeksSocialNetworkAPI.dto.comment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    @NotNull
    private UUID id;

    @NotNull
    private LocalDateTime date;

    @NotNull
    private String text;

    @NotNull
    private UUID postId;

    @NotNull
    private UUID userId;

    private UUID parentCommentId;
}
