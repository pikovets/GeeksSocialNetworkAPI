package org.pikovets.GeeksSocialNetworkAPI.service;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.CommentDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostResponse;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Comment;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.PostLike;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@Service
public class PostService {
    private static final String POST_NOT_FOUND = "Post not found";
    private static final String USER_NOT_ALLOWED = "User not allowed to perform this action";
    private static final String OWNER_NOT_FOUND = "Post does not belong to any user or community";
    private static final String USER_NOT_FOUND = "User not found";

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final CommunityService communityService;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final UserCommunityRepository userCommunityRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public PostService(PostRepository postRepository, PostLikeRepository postLikeRepository, CommentRepository commentRepository, UserRepository userRepository, UserService userService, CommunityService communityService, CommunityRepository communityRepository, UserCommunityRepository userCommunityRepository, UserRelationshipRepository userRelationshipRepository, ModelMapper modelMapper) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.communityService = communityService;
        this.communityRepository = communityRepository;
        this.userCommunityRepository = userCommunityRepository;
        this.userRelationshipRepository = userRelationshipRepository;
        this.modelMapper = modelMapper;
    }

    public Mono<PostDTO> getPost(UUID postId) {
        return postRepository.findById(postId).switchIfEmpty(Mono.error(new NotFoundException(POST_NOT_FOUND))).map(this::convertToPostDTO);
    }

    public Mono<PostDTO> createPost(CreatePostRequest createRequest, UUID authorId) {
        Post post = new Post();
        post.setText(createRequest.getText());
        post.setPhotoLink(createRequest.getPhotoLink());

        enrichPost(authorId, post);

        return postRepository.save(post).map(this::convertToPostDTO);
    }

    public Mono<PostDTO> createPost(CreatePostRequest createRequest, UUID authorId, UUID communityId) {
        Post post = new Post();
        post.setText(createRequest.getText());
        post.setPhotoLink(createRequest.getPhotoLink());
        post.setCommunityId(communityId);

        enrichPost(authorId, post);

        return postRepository.save(post).map(this::convertToPostDTO);
    }

    public PostResponse getPosts(UUID entityId) {
        return new PostResponse(userRepository.findById(entityId)
                .flatMapMany(user ->
                        postRepository.findByAuthorIdOrderByDateDesc(entityId)
                                .filter(post -> post.getCommunityId() == null)
                )
                .switchIfEmpty(
                        communityRepository.findById(entityId)
                                .flatMapMany(community ->
                                        postRepository.findByCommunityIdOrderByDateDesc(entityId)
                                )
                )
                .switchIfEmpty(Flux.error(new NotFoundException(POST_NOT_FOUND))).map(this::convertToPostDTO));
    }


    public Mono<Void> deletePost(UUID postId, UUID authUserId) {
        return postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new NotFoundException(POST_NOT_FOUND)))
                .flatMap(post -> {
                    if (post.getAuthorId() != null) {
                        if (post.getAuthorId().equals(authUserId)) {
                            return postRepository.deleteById(postId);
                        } else {
                            return Mono.error(new NotAllowedException(USER_NOT_ALLOWED));
                        }
                    } else if (post.getCommunityId() != null) {
                        UUID communityId = post.getCommunityId();
                        return userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId)
                                .filter(uc -> uc.getUserRole() == CommunityRole.ADMIN || uc.getUserRole() == CommunityRole.MODERATOR)
                                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)))
                                .then(postRepository.deleteById(postId));
                    } else {
                        return Mono.error(new IllegalStateException(OWNER_NOT_FOUND));
                    }
                });
    }


    public Mono<Void> toggleLike(UUID postId, UUID authUserId) {
        Mono<Post> postMono = postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new NotFoundException(POST_NOT_FOUND)));
        Mono<User> userMono = userRepository.findById(authUserId)
                .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)));

        return Mono.zip(postMono, userMono)
                .flatMap(tuple -> {
                    Post post = tuple.getT1();
                    User user = tuple.getT2();
                    return postLikeRepository.findByPostIdAndUserId(postId, authUserId)
                            .flatMap(existingLike -> postLikeRepository.deleteById(existingLike.getId()))
                            .switchIfEmpty(
                                    postLikeRepository.save(new PostLike(postId, authUserId))
                                            .then()
                            );
                });
    }

    public PostResponse getFeed(UUID authUserId) {
        return new PostResponse(userService.getFriends(authUserId)
                .getUsers().flatMap(friend -> postRepository.findByAuthorIdOrderByDateDesc(friend.getId())
                        .concatWith(postRepository.findByAuthorIdOrderByDateDesc(authUserId))
                        .map(this::convertToPostDTO)
                        .sort(Comparator.comparing(PostDTO::getDate).reversed())));
    }

    public Mono<CommentDTO> addComment(UUID postId, Comment comment) {
        return postRepository.findById(postId).switchIfEmpty(Mono.error(new NotFoundException(POST_NOT_FOUND))).flatMap(post -> {
                    enrichComment(comment);
                    comment.setPostId(postId);
                    return commentRepository.save(comment).map(this::convertToCommentDTO);
                });
    }

    public void enrichPost(UUID authorID, Post post) {
        post.setAuthorId(authorID);
        post.setDate(LocalDateTime.now());
    }

    public CommentDTO enrichComment(Comment comment) {
        comment.setDate(LocalDateTime.now());
        return convertToCommentDTO(comment);
    }

    private PostDTO convertToPostDTO(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }

    private CommentDTO convertToCommentDTO(Comment comment) {
        return modelMapper.map(comment, CommentDTO.class);
    }
}