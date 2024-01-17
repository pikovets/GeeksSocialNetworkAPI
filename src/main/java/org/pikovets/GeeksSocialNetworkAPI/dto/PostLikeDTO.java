package org.pikovets.GeeksSocialNetworkAPI.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLikeDTO {
    @NotNull
    private UserDTO user;
}