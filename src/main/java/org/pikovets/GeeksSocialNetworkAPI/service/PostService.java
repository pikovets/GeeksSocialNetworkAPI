package org.pikovets.GeeksSocialNetworkAPI.service;

import io.jsonwebtoken.lang.Strings;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.comment.CommentDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.comment.CreateCommentRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
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
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final UserCommunityRepository userCommunityRepository;
    private final ModelMapper modelMapper;
    private final TransactionalOperator transactionalOperator;

    @Autowired
    public PostService(PostRepository postRepository, PostLikeRepository postLikeRepository, CommentRepository commentRepository, UserRepository userRepository, UserService userService, CommunityRepository communityRepository, UserCommunityRepository userCommunityRepository, ModelMapper modelMapper, TransactionalOperator transactionalOperator) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.communityRepository = communityRepository;
        this.userCommunityRepository = userCommunityRepository;
        this.modelMapper = modelMapper;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<PostDTO> getPost(UUID postId) {
        return postRepository.findById(postId).switchIfEmpty(Mono.error(new NotFoundException(POST_NOT_FOUND))).map(this::convertToPostDTO);
    }

    public Mono<PostDTO> createPost(Mono<CreatePostRequest> createPostRequestMono, UUID authorId) {
        return createPostRequestMono.flatMap(createPostRequest -> {
            Post post = new Post();
            post.setText(createPostRequest.getText());
            post.setPhotoLink(createPostRequest.getPhotoLink());

            enrichPost(authorId, post);

            return postRepository.save(post).as(transactionalOperator::transactional).map(this::convertToPostDTO);
        });
    }

    public Mono<PostDTO> createPost(Mono<CreatePostRequest> createPostRequestMono, UUID authorId, UUID communityId) {
        return createPostRequestMono.flatMap(createPostRequest -> {
            Post post = new Post();
            if (Strings.hasText(post.getText())) {
                post.setText(createPostRequest.getText());
            }
            if (Strings.hasText(post.getPhotoLink())) {
                post.setPhotoLink(createPostRequest.getPhotoLink());
            }
            post.setCommunityId(communityId);

            enrichPost(authorId, post);

            return postRepository.save(post).as(transactionalOperator::transactional).map(this::convertToPostDTO);
        });
    }

    public Flux<PostDTO> getPosts(UUID entityId) {
        return userRepository.findById(entityId)
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
                .switchIfEmpty(Flux.error(new NotFoundException(POST_NOT_FOUND))).map(this::convertToPostDTO);
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
                })
                .as(transactionalOperator::transactional);
    }


    public Mono<Void> toggleLike(UUID postId, UUID authUserId) {
        Mono<Post> postMono = postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new NotFoundException(POST_NOT_FOUND)));
        Mono<User> userMono = userRepository.findById(authUserId)
                .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)));

        return Mono.zip(postMono, userMono)
                .flatMap(ignored ->
                        postLikeRepository.findByPostIdAndUserId(postId, authUserId)
                                .flatMap(existingLike -> postLikeRepository.deleteById(existingLike.getId()))
                                .switchIfEmpty(
                                        postLikeRepository.save(new PostLike(postId, authUserId)).then()
                                )
                );
    }

    public Flux<PostDTO> getFeed(UUID authUserId) {
        return userService.getFriends(authUserId).onErrorResume(e -> Flux.empty())
                .flatMap(friend -> postRepository.findByAuthorIdOrderByDateDesc(friend.getId())
                        .concatWith(postRepository.findByAuthorIdOrderByDateDesc(authUserId))
                        .map(this::convertToPostDTO)
                        .sort(Comparator.comparing(PostDTO::getDate).reversed()));
    }

    public Mono<CommentDTO> addComment(UUID postId, UUID authUserId, Mono<CreateCommentRequest> commentRequestMono) {
        return commentRequestMono.flatMap(commentRequest -> postRepository.findById(postId).switchIfEmpty(Mono.error(new NotFoundException(POST_NOT_FOUND))).flatMap(post -> {
                    return commentRepository.save(new Comment(commentRequest.getText(), commentRequest.getParentCommentId(), postId, authUserId))
                            .as(transactionalOperator::transactional)
                            .map(this::convertToCommentDTO);
                }
        ));
    }

    public void enrichPost(UUID authorID, Post post) {
        post.setAuthorId(authorID);
    }

    private PostDTO convertToPostDTO(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }

    public CommentDTO convertToCommentDTO(Comment comment) {
        return modelMapper.map(comment, CommentDTO.class);
    }
}