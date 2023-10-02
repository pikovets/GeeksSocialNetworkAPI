package org.pikovets.GeeksSocialNetworkAPI.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserResponse {
    private List<UserDTO> users;
}