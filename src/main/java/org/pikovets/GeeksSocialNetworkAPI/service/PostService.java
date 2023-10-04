package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.UpdatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.UnAuthorizedException;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    public PostService(PostRepository postRepository, UserService userService, JwtUtils jwtUtils) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public void createPost(CreatePostRequest createRequest, String token) {
        Post post = new Post();
        User author = userService.getUserByEmail(jwtUtils.extractUsername(token));
        enrichPost(author, post, createRequest);

        postRepository.save(post);
    }

    public Post getPostById(UUID id) {
        return postRepository.findById(id).orElseThrow(new NotFoundException("User not found"));
    }

    @Transactional
    public void updatePost(UUID id, UpdatePostRequest updateRequest, String token) {
        Post postToBeUpdated = postRepository.findById(id).orElseThrow(new NotFoundException("Post not found"));

        validateDeletePermission(postToBeUpdated, token);

        Post updatedPost = new Post();
        updatedPost.setText(updateRequest.getText());
        updatedPost.setId(id);
        updatedPost.setDate(postToBeUpdated.getDate());
        updatedPost.setAuthor(postToBeUpdated.getAuthor());

        postRepository.save(updatedPost);
    }

    @Transactional
    public void deletePost(UUID id, String token) {
        Post deletedPost = postRepository.findById(id)
                .orElseThrow(new NotFoundException("Post not found"));

        validateDeletePermission(deletedPost, token);

        postRepository.delete(deletedPost);
    }

    private void enrichPost(User author, Post post, CreatePostRequest createRequest) {
        post.setText(createRequest.getText());
        post.setAuthor(userService.getUserById(author.getId()));
        post.setDate(LocalDateTime.now());
    }

    private void validateDeletePermission(Post deletedPost, String token) {
        if (deletedPost.getAuthor().getEmail().equals(jwtUtils.extractUsername(token))) {
            throw new UnAuthorizedException();
        }
    }
}