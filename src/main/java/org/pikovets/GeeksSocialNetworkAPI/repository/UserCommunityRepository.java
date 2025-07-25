package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserCommunityRepository extends ReactiveCrudRepository<UserCommunity, UUID> {
    @Query("SELECT * FROM user_community uc WHERE (uc.user_id = :userId) AND (uc.community_id = :communityId)")
    Mono<UserCommunity> findByCommunityIdAndUserId(UUID communityId, UUID userId);

    @Query("SELECT * FROM user_community uc WHERE (uc.community_id = :communityId)")
    Flux<UserCommunity> findByCommunityId(UUID communityId);

    @Query("SELECT * FROM user_community uc WHERE (uc.user_id = :userId)")
    Flux<UserCommunity> findByUserId(UUID userId);

    @Query("DELETE FROM user_community uc WHERE (uc.user_id = :userId) AND (uc.community_id = :communityId)")
    Mono<Integer> deleteByCommunityIdAndUserId(UUID communityId, UUID userId);
}
