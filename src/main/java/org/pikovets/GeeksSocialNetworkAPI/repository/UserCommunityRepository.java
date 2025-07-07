package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserCommunityRepository extends ReactiveCrudRepository<UserCommunity, UUID> {
    Mono<UserCommunity> findByCommunityIdAndUserId(UUID communityId, UUID userId);

    Flux<UserCommunity> findByCommunityId(UUID communityId);

    Flux<UserCommunity> findByUserId(UUID userId);

    Flux<Void> deleteByCommunityIdAndUserId(UUID communityId, UUID userId);
}
