package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.CommentLike;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CommentLikeRepository extends ReactiveCrudRepository<CommentLike, UUID> {
    Mono<CommentLike> findByCommentIdAndUserId(UUID commentId, UUID userId);
}
