package org.pikovets.GeeksSocialNetworkAPI.security;

import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IAuthenticationFacade {
    Mono<Authentication> getAuthentication();

    Mono<UUID> getUserID();
}
