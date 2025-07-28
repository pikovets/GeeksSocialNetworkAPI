package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Comment;
import org.pikovets.GeeksSocialNetworkAPI.model.CommentLike;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentLikeRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentRepository;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest {
    private CommentLikeRepository commentLikeRepository;
    private CommentRepository commentRepository;
    private TransactionalOperator transactionalOperator;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentLikeRepository = mock(CommentLikeRepository.class);
        commentRepository = mock(CommentRepository.class);
        transactionalOperator = mock(TransactionalOperator.class);

        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        commentService = new CommentService(commentRepository, commentLikeRepository, transactionalOperator);
    }

    @Test
    void toggleCommentLike_whenLikeExists_thenDeleteIt() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentLike existingLike = new CommentLike();
        existingLike.setId(UUID.randomUUID());
        existingLike.setCommentId(commentId);
        existingLike.setUserId(userId);

        when(commentLikeRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(Mono.just(existingLike));
        when(commentLikeRepository.deleteById(existingLike.getId()))
                .thenReturn(Mono.empty());

        when(commentLikeRepository.save(any(CommentLike.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(commentService.toggleCommentLike(commentId, userId))
                .verifyComplete();

    }


    @Test
    void toggleCommentLike_whenLikeDoesNotExist_thenCreateIt() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentLikeRepository.findByCommentIdAndUserId(commentId, userId)).thenReturn(Mono.empty());
        when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(Mono.just(new CommentLike()));

        StepVerifier.create(commentService.toggleCommentLike(commentId, userId))
                .verifyComplete();

        verify(commentLikeRepository).save(argThat(cl -> cl.getCommentId().equals(commentId) && cl.getUserId().equals(userId)));
        verify(commentLikeRepository, never()).deleteById(commentId);
    }

    @Test
    void deleteComment_whenCommentExistsAndUserIsOwner_thenDelete() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUserId(userId);

        when(commentRepository.findById(commentId)).thenReturn(Mono.just(comment));
        when(commentRepository.deleteById(commentId)).thenReturn(Mono.empty());

        StepVerifier.create(commentService.deleteComment(commentId, userId))
                .verifyComplete();

        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void deleteComment_whenCommentNotFound_thenError() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentRepository.findById(commentId)).thenReturn(Mono.empty());

        StepVerifier.create(commentService.deleteComment(commentId, userId))
                .expectErrorMatches(e -> e instanceof NotFoundException && e.getMessage().equals("Comment not found"))
                .verify();

        verify(commentRepository, never()).deleteById(commentId);
    }

    @Test
    void deleteComment_whenUserNotOwner_thenError() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUserId(UUID.randomUUID());

        when(commentRepository.findById(commentId)).thenReturn(Mono.just(comment));

        StepVerifier.create(commentService.deleteComment(commentId, userId))
                .expectErrorMatches(e -> e instanceof NotAllowedException && e.getMessage().equals("User not allowed to perform this action"))
                .verify();

        verify(commentRepository, never()).deleteById(commentId);
    }
}
