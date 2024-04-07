package org.pikovets.GeeksSocialNetworkAPI.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class CommentDTO {
    @NotNull
    private UUID id;

    @NotNull
    private LocalDateTime date;

    @NotNull
    private String text;

    @NotNull
    private UserDTO author;

    private CommentDTO parentComment;

    private Set<CommentLikeDTO> likes;
}
