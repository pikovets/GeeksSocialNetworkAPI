package org.pikovets.GeeksSocialNetworkAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String token;
}