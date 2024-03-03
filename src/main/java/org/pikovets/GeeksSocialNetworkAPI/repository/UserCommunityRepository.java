package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCommunityRepository extends JpaRepository<UserCommunity, UUID> {
    Optional<UserCommunity> findByUserIdAndCommunityId(UUID userId, UUID communityId);
}
