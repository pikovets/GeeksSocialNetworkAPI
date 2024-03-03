package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommunityRepository extends JpaRepository<Community, UUID> {
}
