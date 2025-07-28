package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.CommentLike;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentLikeRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class CommentService {
    private static final String COMMENT_NOT_FOUND = "Comment not found";
    private static final String USER_NOT_ALLOWED = "User not allowed to perform this action";

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final TransactionalOperator transactionalOperator;

    @Autowired
    public CommentService(CommentRepository commentRepository, CommentLikeRepository commentLikeRepository, TransactionalOperator transactionalOperator) {
        this.commentLikeRepository = commentLikeRepository;
        this.commentRepository = commentRepository;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<Void> toggleCommentLike(UUID commentId, UUID authUserId) {
        return commentLikeRepository.findByCommentIdAndUserId(commentId, authUserId)
                .flatMap(commentLike -> {
                    return commentLikeRepository.deleteById(commentLike.getId())
                            .then();
                })
                .switchIfEmpty(
                        Mono.defer(() -> {
                            CommentLike commentLike = new CommentLike();
                            commentLike.setCommentId(commentId);
                            commentLike.setUserId(authUserId);
                            return commentLikeRepository.save(commentLike)
                                    .then();
                        })
                )
                .as(transactionalOperator::transactional);
    }




    public Mono<Void> deleteComment(UUID commentId, UUID authUserId) {
        return commentRepository.findById(commentId)
                .switchIfEmpty(Mono.error(new NotFoundException(COMMENT_NOT_FOUND)))
                .filter(comment -> comment.getUserId().equals(authUserId))
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)))
                .flatMap(comment -> commentRepository.deleteById(commentId))
                .as(transactionalOperator::transactional);
    }
}
