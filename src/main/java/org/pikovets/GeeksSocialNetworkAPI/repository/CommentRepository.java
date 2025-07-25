package org.pikovets.GeeksSocialNetworkAPI.repository;

import org.pikovets.GeeksSocialNetworkAPI.model.Comment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface CommentRepository extends ReactiveCrudRepository<Comment, UUID> {
    Flux<Comment> findByPostId(String postId);
}
