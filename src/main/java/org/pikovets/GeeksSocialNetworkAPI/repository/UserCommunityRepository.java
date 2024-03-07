package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCommunityRepository extends JpaRepository<UserCommunity, UUID> {
    Optional<UserCommunity> findByCommunityIdAndUserId(UUID communityId, UUID userId);
}
