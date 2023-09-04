package org.pikovets.GeeksSocialNetworkAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.pikovets.GeeksSocialNetworkAPI.model.User;

import java.util.List;

@Data
@AllArgsConstructor
public class UserResponse {
    private List<UserDTO> users;
}