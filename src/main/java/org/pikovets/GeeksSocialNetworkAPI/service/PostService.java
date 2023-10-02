package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostRepository;
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

    public void enrichPost(UUID authorID, Post post, CreatePostRequest createRequest) {
        post.setText(createRequest.getText());
        post.setAuthor(userService.getUserById(authorID));
        post.setDate(LocalDateTime.now());
    }
}