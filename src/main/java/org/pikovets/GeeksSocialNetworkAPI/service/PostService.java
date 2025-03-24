package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.*;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostLikeRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final CommunityService communityService;
    private final UserCommunityRepository userCommunityRepository;

    @Autowired
    public PostService(PostRepository postRepository, PostLikeRepository postLikeRepository, CommentRepository commentRepository, UserService userService, CommunityService communityService, UserCommunityRepository userCommunityRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.communityService = communityService;
        this.userCommunityRepository = userCommunityRepository;
    }

    public Post getPost(UUID postId) {
        return postRepository.findById(postId).orElseThrow(new NotFoundException("Post not found"));
    }

    @Transactional
    public void createPost(CreatePostRequest createRequest, UUID authorId) {
        Post post = new Post();
        post.setText(createRequest.getText());
        post.setPhotoLink(createRequest.getPhotoLink());

        enrichPost(authorId, post);

        postRepository.save(post);
    }

    @Transactional
    public void createPost(CreatePostRequest createRequest, UUID authorId, UUID communityId) {
        Post post = new Post();
        post.setText(createRequest.getText());
        post.setPhotoLink(createRequest.getPhotoLink());
        post.setCommunity(communityService.getById(communityId));

        enrichPost(authorId, post);

        postRepository.save(post);
    }

    public List<Post> getPosts(UUID entityId) {
        User user = userService.getUserById(entityId);

        if (user == null) {
            Community community = communityService.getById(entityId);

            return postRepository.findByCommunityOrderByDateDesc(community);
        }

        return postRepository.findByAuthorOrderByDateDesc(user).stream().filter(post -> post.getCommunity() == null).toList();
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        User user = userService.getUserById(userId);

        Post post = postRepository.findById(postId).orElseThrow(new NotFoundException("Post not found"));

        Community postCommunity = post.getCommunity();
        if (postCommunity != null) {
            Optional<UserCommunity> optionalUserCommunity = userCommunityRepository.findByCommunityIdAndUserId(postCommunity.getId(), userId);
            if (optionalUserCommunity.isPresent()) {
                UserCommunity userCommunity = optionalUserCommunity.get();
                CommunityRole userRole = userCommunity.getUserRole();
                if (userRole == CommunityRole.ADMIN || userRole == CommunityRole.MODERATOR) {
                    postRepository.delete(post);
                    return;
                }
            }
        } else if (post.getAuthor().equals(user)) {
            postRepository.delete(post);
            return;
        }

        throw new NotAllowedException("You do not have permission to delete this post");
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

    public List<Post> getFeed(UUID authUserId) {
        return Stream.concat(userService.getFriends(authUserId).stream()
                        .flatMap(user -> user.getPosts().stream()), getPosts(authUserId).stream())
                .sorted(Comparator.comparing(Post::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public void addComment(UUID postId, Comment comment) {
        Post post = postRepository.findById(postId).orElseThrow(new NotFoundException("Post not found"));
        enrichComment(comment);

        comment.setPost(post);
        post.getComments().add(comment);

        commentRepository.save(comment);
    }

    public void enrichPost(UUID authorID, Post post) {
        post.setAuthor(userService.getUserById(authorID));
        post.setDate(LocalDateTime.now());
    }

    public void enrichComment(Comment comment) {
        comment.setDate(LocalDateTime.now());
    }
}