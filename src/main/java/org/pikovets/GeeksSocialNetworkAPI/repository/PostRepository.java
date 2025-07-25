package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface PostRepository extends ReactiveCrudRepository<Post, UUID> {
    Flux<Post> findByAuthorIdOrderByDateDesc(UUID authorId);

    Flux<Post> findByCommunityIdOrderByDateDesc(UUID communityId);
}