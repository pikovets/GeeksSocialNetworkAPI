package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.*;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentLikeRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CommentService {
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    @Autowired
    public CommentService(CommentRepository commentRepository, CommentLikeRepository commentLikeRepository, CommentRepository commentRepository1, UserRepository userRepository, UserService userService) {
        this.commentLikeRepository = commentLikeRepository;
        this.commentRepository = commentRepository1;
        this.userService = userService;
    }

    @Transactional
    public void toggleCommentLike(UUID commentId, UUID authUserId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(new NotFoundException("Comment not found"));
        User authUser = userService.getUserById(authUserId);

        Optional<CommentLike> existedCommentLike = comment
                .getLikes()
                .stream()
                .filter(like -> like.getUser().getId().equals(authUserId))
                .findAny();

        if (existedCommentLike.isPresent()) {
            commentLikeRepository.deleteById(existedCommentLike.get().getId());
        } else {
            CommentLike commentLike = new CommentLike();
            commentLike.setComment(comment);
            commentLike.setUser(authUser);

            comment.getLikes().add(commentLike);
            commentLikeRepository.save(commentLike);
        }
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID authUserId) {
        Comment commentToDelete = commentRepository.findById(commentId).orElseThrow(new NotFoundException("Comment not found"));

        if (!commentToDelete.getAuthor().getId().equals(authUserId))
            throw new NotAllowedException("The comment does not belong to you");

        commentRepository.delete(commentToDelete);
    }
}
