package org.pikovets.GeeksSocialNetworkAPI.controllers;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostResponse;
import org.pikovets.GeeksSocialNetworkAPI.model.Comment;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
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
    private final ModelMapper modelMapper;

    @Autowired
    public PostController(PostService postService, IAuthenticationFacade authenticationFacade, ModelMapper modelMapper) {
        this.postService = postService;
        this.authenticationFacade = authenticationFacade;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable("id") UUID postId) {
        return new ResponseEntity<>(convertToPostDTO(postService.getPost(postId)), HttpStatus.OK);
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

    @PostMapping("/{id}/addComment")
    public ResponseEntity<HttpStatus> addComment(@PathVariable("id") UUID postId, @RequestBody Comment comment) {
        postService.addComment(postId, comment);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/feed")
    public ResponseEntity<PostResponse> getFeed() {
        return new ResponseEntity<>(new PostResponse(postService.getFeed(authenticationFacade.getUserID()).stream().map(this::convertToPostDTO).toList()), HttpStatus.OK);
    }

    public PostDTO convertToPostDTO(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }
}
