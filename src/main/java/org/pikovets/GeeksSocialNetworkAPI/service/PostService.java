package org.pikovets.GeeksSocialNetworkAPI.service;

import org.hibernate.annotations.NotFound;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.UpdatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;

    @Autowired
    public PostService(PostRepository postRepository, UserService userService) {
        this.postRepository = postRepository;
        this.userService = userService;
    }

    @Transactional
    public void createPost(UUID authorId, CreatePostRequest createRequest) {
        Post post = new Post();
        enrichPost(authorId, post, createRequest);

        postRepository.save(post);
    }

    public Post getPostById(UUID id) {
        return postRepository.findById(id).orElseThrow(new NotFoundException("User not found"));
    }

    @Transactional
    public void updatePost(UUID id, UpdatePostRequest updateRequest)
    {
        Post postToBeUpdated = postRepository.findById(id).orElseThrow(new NotFoundException("Post not found"));

        Post updatedPost = new Post();
        updatedPost.setText(updateRequest.getText());
        updatedPost.setId(id);
        updatedPost.setDate(postToBeUpdated.getDate());
        updatedPost.setAuthor(postToBeUpdated.getAuthor());

        postRepository.save(updatedPost);
    }

    @Transactional
    public void deletePost(UUID id) {
        Post deletedPost = postRepository.findById(id)
                .orElseThrow(new NotFoundException("Post not found"));

        postRepository.delete(deletedPost);
    }

    public void enrichPost(UUID authorID, Post post, CreatePostRequest createRequest) {
        post.setText(createRequest.getText());
        post.setAuthor(userService.getUserById(authorID));
        post.setDate(LocalDateTime.now());
    }
}