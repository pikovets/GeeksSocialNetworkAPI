package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.PostLike;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PostLikeRepository extends ReactiveCrudRepository<PostLike, UUID> {
    Mono<Post> findByPostIdAndUserId(UUID postId, UUID userId);

}
