package org.pikovets.GeeksSocialNetworkAPI.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {
    @Override
    public Mono<UUID> getUserID() {
        return getAuthentication()
                .map(auth -> UUID.fromString(auth.getName()));
    }

    @Override
    public Mono<Authentication> getAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication);
    }
}