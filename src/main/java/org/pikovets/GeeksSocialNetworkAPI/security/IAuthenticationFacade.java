package org.pikovets.GeeksSocialNetworkAPI.security;

import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface IAuthenticationFacade {
    Authentication getAuthentication();

    UUID getUserID();
}
