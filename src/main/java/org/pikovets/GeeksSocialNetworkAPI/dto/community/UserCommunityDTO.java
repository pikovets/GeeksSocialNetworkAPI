package org.pikovets.GeeksSocialNetworkAPI.dto.community;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCommunityDTO {
    @NotNull
    private UserDTO user;

    @NotNull
    private CommunityDTO community;

    @NotNull
    private CommunityRole userRole;

    @Override
    public int hashCode() {
        return user.hashCode() + community.hashCode();
    }
}