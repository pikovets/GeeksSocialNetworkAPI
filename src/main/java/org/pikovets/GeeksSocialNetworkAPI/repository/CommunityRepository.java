package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface CommunityRepository extends ReactiveCrudRepository<Community, UUID> {
    Flux<Community> findByNameIgnoreCase(String name);

    Flux<Community> findByNameContainingIgnoreCase(String name);
}
