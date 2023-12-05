package org.pikovets.GeeksSocialNetworkAPI.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {
    @Override
    public UUID getUserID() {
        return UUID.fromString(getAuthentication().getName());
    }

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}