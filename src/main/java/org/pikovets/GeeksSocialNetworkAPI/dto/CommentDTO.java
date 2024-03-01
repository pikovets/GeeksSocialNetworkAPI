package org.pikovets.GeeksSocialNetworkAPI.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;

import java.util.UUID;

@Data
public class CommentDTO {
    @NotNull
    private UUID id;

    @NotNull
    private String text;

    @NotNull
    private UserDTO author;

    @NotNull
    private CommentDTO parentComment;
}
