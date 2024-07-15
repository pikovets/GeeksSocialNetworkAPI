package org.pikovets.GeeksSocialNetworkAPI.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;

import java.util.UUID;

@Data
public class CommentLikeDTO {
    @NotNull
    private UUID id;

    @NotNull
    private UserDTO user;
}
