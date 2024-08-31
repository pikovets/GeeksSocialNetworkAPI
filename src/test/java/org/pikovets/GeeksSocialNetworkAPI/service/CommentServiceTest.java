package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Comment;
import org.pikovets.GeeksSocialNetworkAPI.model.CommentLike;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentLikeRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CommentServiceTest {

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentService commentService;

    private UUID commentId;
    private UUID userId;
    private Comment comment;
    private User authUser;
    private CommentLike commentLike;
    private Post post;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();

        comment = new Comment();
        comment.setId(commentId);
        comment.setText("Test text");

        authUser = new User();
        authUser.setId(userId);
        authUser.setEmail("testUser@gmail.com");

        post = new Post();
        post.setId(UUID.randomUUID());
        post.setAuthor(authUser);

        comment.setAuthor(authUser);
        comment.setPost(post);
        commentLike = new CommentLike();
        commentLike.setId(UUID.randomUUID());
        commentLike.setComment(comment);
        commentLike.setUser(authUser);
    }

    @Test
    public void testToggleCommentLike_ExistingLike() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userService.getUserById(userId)).thenReturn(authUser);
        comment.getLikes().add(commentLike);

        commentService.toggleCommentLike(commentId, userId);

        verify(commentLikeRepository).deleteById(commentLike.getId());
        verify(commentLikeRepository, never()).save(any(CommentLike.class));
    }

    @Test
    public void testToggleCommentLike_NewLike() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userService.getUserById(userId)).thenReturn(authUser);

        commentService.toggleCommentLike(commentId, userId);

        verify(commentLikeRepository, never()).deleteById(any(UUID.class));
        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    @Test
    public void testToggleCommentLike_CommentNotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            commentService.toggleCommentLike(commentId, userId);
        });

        assertEquals("Comment not found", exception.getMessage());
    }

    @Test
    public void testDeleteComment_Success() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(commentId, userId);

        verify(commentRepository).delete(comment);
    }

    @Test
    public void testDeleteComment_NotAuthor() {
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        comment.setAuthor(anotherUser);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        NotAllowedException exception = assertThrows(NotAllowedException.class, () -> {
            commentService.deleteComment(commentId, userId);
        });

        assertEquals("The comment does not belong to you", exception.getMessage());
    }

    @Test
    public void testDeleteComment_CommentNotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            commentService.deleteComment(commentId, userId);
        });

        assertEquals("Comment not found", exception.getMessage());
    }
}