package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRelationshipRepository extends JpaRepository<UserRelationship, UUID> {
    @Query(value = "select ur from UserRelationship ur where (ur.requester.id = ?1 and ur.acceptor.id = ?2) or (ur.requester.id = ?2 and ur.acceptor.id = ?1)")
    Optional<UserRelationship> findByRequesterIdAndAcceptorId(UUID requesterId, UUID acceptorId);

    @Query(value = "select acceptor from UserRelationship ur where ur.requester.id = ?1 or ur.acceptor.id = ?1")
    List<UserRelationship> getFriends(UUID userId);
}
