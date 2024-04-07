package org.pikovets.GeeksSocialNetworkAPI.controllers;

import org.pikovets.GeeksSocialNetworkAPI.security.AuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
public class CommentController {
    private final AuthenticationFacade authenticationFacade;
    private final CommentService commentService;

    @Autowired
    public CommentController(AuthenticationFacade authenticationFacade, CommentService commentService) {
        this.authenticationFacade = authenticationFacade;
        this.commentService = commentService;
    }

    @PostMapping("/{id}/toggleLike")
    private ResponseEntity<HttpStatus> toggleCommentLike(@PathVariable("id") UUID commentId) {
        commentService.toggleCommentLike(commentId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<HttpStatus> deleteComment(@PathVariable("id") UUID commentId) {
        commentService.deleteComment(commentId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
