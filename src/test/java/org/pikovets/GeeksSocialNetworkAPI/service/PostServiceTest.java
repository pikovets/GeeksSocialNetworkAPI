package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.comment.CommentDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.comment.CreateCommentRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.*;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.repository.*;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class PostServiceTest {
    private PostRepository postRepository;
    private PostLikeRepository postLikeRepository;
    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private UserService userService;
    private CommunityRepository communityRepository;
    private UserCommunityRepository userCommunityRepository;
    private ModelMapper modelMapper;
    private TransactionalOperator transactionalOperator;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postRepository = mock(PostRepository.class);
        postLikeRepository = mock(PostLikeRepository.class);
        commentRepository = mock(CommentRepository.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        communityRepository = mock(CommunityRepository.class);
        userCommunityRepository = mock(UserCommunityRepository.class);
        modelMapper = mock(ModelMapper.class);
        transactionalOperator = mock(TransactionalOperator.class);

        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        postService = new PostService(
                postRepository,
                postLikeRepository,
                commentRepository,
                userRepository,
                userService,
                communityRepository,
                userCommunityRepository,
                modelMapper,
                transactionalOperator
        );
    }

    @Test
    void getPost_found() {
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        PostDTO postDTO = new PostDTO();

        when(postRepository.findById(postId)).thenReturn(Mono.just(post));
        when(modelMapper.map(post, PostDTO.class)).thenReturn(postDTO);

        StepVerifier.create(postService.getPost(postId))
                .expectNext(postDTO)
                .verifyComplete();
    }

    @Test
    void getPost_notFound() {
        UUID postId = UUID.randomUUID();

        when(postRepository.findById(postId)).thenReturn(Mono.empty());

        StepVerifier.create(postService.getPost(postId))
                .expectErrorMatches(e -> e instanceof NotFoundException && e.getMessage().equals("Post not found"))
                .verify();
    }

    @Test
    void createPost_withoutCommunity_success() {
        UUID authorId = UUID.randomUUID();
        CreatePostRequest request = new CreatePostRequest();
        request.setText("text");
        request.setPhotoLink("photoLink");

        Post savedPost = new Post();
        PostDTO postDTO = new PostDTO();

        when(postRepository.save(any(Post.class))).thenReturn(Mono.just(savedPost));
        when(modelMapper.map(savedPost, PostDTO.class)).thenReturn(postDTO);

        StepVerifier.create(postService.createPost(Mono.just(request), authorId))
                .expectNext(postDTO)
                .verifyComplete();

        verify(postRepository).save(argThat(post ->
                "text".equals(post.getText()) &&
                        "photoLink".equals(post.getPhotoLink()) &&
                        authorId.equals(post.getAuthorId())
        ));
    }

    @Test
    void createPost_withCommunity_success() {
        UUID authorId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        CreatePostRequest request = new CreatePostRequest();
        request.setText("text");
        request.setPhotoLink("photoLink");

        Post savedPost = new Post();
        PostDTO postDTO = new PostDTO();

        when(postRepository.save(any(Post.class))).thenReturn(Mono.just(savedPost));
        when(modelMapper.map(savedPost, PostDTO.class)).thenReturn(postDTO);

        StepVerifier.create(postService.createPost(Mono.just(request), authorId, communityId))
                .expectNext(postDTO)
                .verifyComplete();

        verify(postRepository).save(argThat(post ->
                communityId.equals(post.getCommunityId()) &&
                        authorId.equals(post.getAuthorId())
        ));
    }

    @Test
    void getPosts_userPosts() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        Post post1 = new Post();
        post1.setCommunityId(null);
        Post post2 = new Post();
        post2.setCommunityId(null);
        PostDTO dto1 = new PostDTO();
        PostDTO dto2 = new PostDTO();

        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(postRepository.findByAuthorIdOrderByDateDesc(userId)).thenReturn(Flux.just(post1, post2));
        when(modelMapper.map(post1, PostDTO.class)).thenReturn(dto1);
        when(modelMapper.map(post2, PostDTO.class)).thenReturn(dto2);

        when(communityRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(postService.getPosts(userId))
                .expectNext(dto1, dto2)
                .verifyComplete();
    }


    @Test
    void getPosts_communityPosts() {
        UUID communityId = UUID.randomUUID();
        Community community = new Community();
        Post post = new Post();
        PostDTO dto = new PostDTO();

        when(userRepository.findById(communityId)).thenReturn(Mono.empty());
        when(communityRepository.findById(communityId)).thenReturn(Mono.just(community));
        when(postRepository.findByCommunityIdOrderByDateDesc(communityId)).thenReturn(Flux.just(post));
        when(modelMapper.map(post, PostDTO.class)).thenReturn(dto);

        StepVerifier.create(postService.getPosts(communityId))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void getPosts_notFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Mono.empty());
        when(communityRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(postService.getPosts(id))
                .expectErrorMatches(e -> e instanceof NotFoundException && e.getMessage().equals("Post not found"))
                .verify();
    }

    @Test
    void deletePost_authorSuccess() {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        Post post = new Post();
        post.setAuthorId(authorId);

        when(postRepository.findById(postId)).thenReturn(Mono.just(post));
        when(postRepository.deleteById(postId)).thenReturn(Mono.empty());

        StepVerifier.create(postService.deletePost(postId, authorId))
                .verifyComplete();
    }

    @Test
    void deletePost_authorNotAllowed() {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Post post = new Post();
        post.setAuthorId(otherUserId);

        when(postRepository.findById(postId)).thenReturn(Mono.just(post));

        StepVerifier.create(postService.deletePost(postId, authorId))
                .expectErrorMatches(e -> e instanceof NotAllowedException && e.getMessage().equals("User not allowed to perform this action"))
                .verify();
    }

    @Test
    void deletePost_communityAdminSuccess() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        Post post = new Post();
        post.setCommunityId(communityId);

        UserCommunity adminUC = new UserCommunity(userId, communityId, CommunityRole.ADMIN);

        when(postRepository.findById(postId)).thenReturn(Mono.just(post));
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId)).thenReturn(Mono.just(adminUC));
        when(postRepository.deleteById(postId)).thenReturn(Mono.empty());

        StepVerifier.create(postService.deletePost(postId, userId))
                .verifyComplete();
    }

    @Test
    void deletePost_communityNotAllowed() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        Post post = new Post();
        post.setCommunityId(communityId);

        UserCommunity memberUC = new UserCommunity(userId, communityId, CommunityRole.MEMBER);

        when(postRepository.findById(postId)).thenReturn(Mono.just(post));
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId)).thenReturn(Mono.just(memberUC));

        when(postRepository.deleteById(postId)).thenReturn(Mono.empty());

        StepVerifier.create(postService.deletePost(postId, userId))
                .expectErrorMatches(e -> e instanceof NotAllowedException && e.getMessage().equals("User not allowed to perform this action"))
                .verify();
    }


    @Test
    void deletePost_ownerNotFound() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post post = new Post();
        post.setAuthorId(null);
        post.setCommunityId(null);

        when(postRepository.findById(postId)).thenReturn(Mono.just(post));

        StepVerifier.create(postService.deletePost(postId, userId))
                .expectErrorMatches(e -> e instanceof IllegalStateException && e.getMessage().equals("Post does not belong to any user or community"))
                .verify();
    }

    @Test
    void toggleLike_removesExistingLike() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post post = new Post();
        User user = new User();

        PostLike existingLike = new PostLike(postId, userId);
        existingLike.setId(UUID.randomUUID());

        when(postRepository.findById(postId)).thenReturn(Mono.just(post));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(postLikeRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Mono.just(existingLike));
        when(postLikeRepository.deleteById(existingLike.getId())).thenReturn(Mono.empty());
        when(postLikeRepository.save(any())).thenReturn(Mono.empty());

        StepVerifier.create(postService.toggleLike(postId, userId))
                .verifyComplete();
    }

    @Test
    void toggleLike_addsLikeIfNotExists() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post post = new Post();
        User user = new User();

        when(postRepository.findById(postId)).thenReturn(Mono.just(post));
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(postLikeRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Mono.empty());
        when(postLikeRepository.save(any(PostLike.class))).thenReturn(Mono.just(new PostLike(postId, userId)));

        StepVerifier.create(postService.toggleLike(postId, userId))
                .verifyComplete();
    }

    @Test
    void addComment_success() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateCommentRequest request = new CreateCommentRequest();
        request.setText("comment text");
        request.setParentCommentId(null);

        Post post = new Post();
        Comment savedComment = new Comment();
        CommentDTO commentDTO = new CommentDTO();

        when(postRepository.findById(postId)).thenReturn(Mono.just(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(Mono.just(savedComment));
        when(modelMapper.map(savedComment, CommentDTO.class)).thenReturn(commentDTO);

        StepVerifier.create(postService.addComment(postId, userId, Mono.just(request)))
                .expectNext(commentDTO)
                .verifyComplete();
    }
}
