package org.pikovets.GeeksSocialNetworkAPI.controllers;

import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final IAuthenticationFacade authenticationFacade;

    @Autowired
    public PostController(PostService postService, IAuthenticationFacade authenticationFacade) {
        this.postService = postService;
        this.authenticationFacade = authenticationFacade;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletePost(@PathVariable("id") UUID postId) {
        postService.deletePost(postId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/toggleLike")
    public ResponseEntity<HttpStatus> toggleLike(@PathVariable("id") UUID postId) {
        postService.toggleLike(postId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
