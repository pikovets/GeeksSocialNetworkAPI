package org.pikovets.GeeksSocialNetworkAPI.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String token;
}