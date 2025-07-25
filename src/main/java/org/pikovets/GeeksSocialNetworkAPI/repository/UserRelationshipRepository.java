package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRelationshipRepository extends ReactiveCrudRepository<UserRelationship, UUID> {
    @Query("SELECT * FROM user_relationship ur WHERE (ur.requester_id = :requesterId AND ur.acceptor_id = :acceptorId) OR (ur.requester_id = :acceptorId AND ur.acceptor_id = :requesterId)")
    Mono<UserRelationship> findByRequesterIdAndAcceptorId(UUID requesterId, UUID acceptorId);

    @Query("SELECT * FROM user_relationship ur WHERE (ur.acceptor_id = :acceptorId AND ur.type = :type")
    Flux<UserRelationship> findByAcceptorIdAndType(UUID acceptorId, RelationshipType type);

    @Query("SELECT * FROM user_relationship ur WHERE ur.requester_id = :userId OR ur.acceptor_id = :userId")
    Flux<UserRelationship> getFriends(UUID userId);

    @Modifying
    @Query("UPDATE user_relationship SET type = :type WHERE requester_id = :requesterId AND acceptor_id = :acceptorId")
    Mono<Void> updateRelationshipType(String type, UUID requesterId, UUID acceptorId);

    @Query("DELETE FROM user_relationship ur WHERE (ur.requester_id = :requesterId AND ur.acceptor_id = :acceptorId) OR (ur.requester_id = :acceptorId AND ur.acceptor_id = :requesterId)")
    Mono<Void> deleteByRequesterIdAndAcceptorId(UUID requesterId, UUID acceptorId);
}