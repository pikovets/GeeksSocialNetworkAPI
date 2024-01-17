package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.PostLike;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostLikeRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserService userService;

    @Autowired
    public PostService(PostRepository postRepository, PostLikeRepository postLikeRepository, UserService userService) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.userService = userService;
    }

    @Transactional
    public void createPost(UUID authorId, CreatePostRequest createRequest) {
        Post post = new Post();
        post.setText(createRequest.getText());
        post.setPhotoLink(createRequest.getPhotoLink());

        enrichPost(authorId, post);

        postRepository.save(post);
    }

    public List<Post> getPosts(UUID authorId) {
        User user = userService.getUserById(authorId);
        return postRepository.findByAuthorOrderByDateDesc(user);
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        User user = userService.getUserById(userId);
        Optional<Post> post = postRepository.findById(postId);

        if (post.isPresent() && post.get().getAuthor().equals(user)) {
            postRepository.delete(post.get());
        }
    }

    @Transactional
    public void toggleLike(UUID postId, UUID authUserId) {
        Post post = postRepository.findById(postId).orElseThrow(new NotFoundException("Post not found"));
        User authUser = userService.getUserById(authUserId);

        Optional<PostLike> existedLike = post
                .getLikes()
                .stream()
                .filter(like -> like.getUser().getId().equals(authUserId))
                .findAny();

        if (existedLike.isPresent()) {
            postLikeRepository.deleteById(existedLike.get().getId());
        } else {
            PostLike postLike = new PostLike();
            postLike.setPost(post);
            postLike.setUser(authUser);

            post.getLikes().add(postLike);
            postLikeRepository.save(postLike);
        }
    }

    public void enrichPost(UUID authorID, Post post) {
        post.setAuthor(userService.getUserById(authorID));
        post.setDate(LocalDateTime.now());
    }
}