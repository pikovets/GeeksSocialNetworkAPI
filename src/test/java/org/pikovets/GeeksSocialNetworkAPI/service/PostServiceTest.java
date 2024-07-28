package org.pikovets.GeeksSocialNetworkAPI.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.CreatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.*;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostLikeRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PostServiceTest {

    private UUID postId;
    private UUID userId;
    private UUID communityId;
    private Post post;
    private User user;
    private Community community;
    private CreatePostRequest createPost;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserService userService;

    @Mock
    private CommunityService communityService;

    @Mock
    private UserCommunityRepository userCommunityRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        user = new User();
        user.setEmail("testUser@gmail.com");

        postId = UUID.randomUUID();
        post = new Post();
        post.setId(postId);
        post.setAuthor(user);

        communityId = UUID.randomUUID();
        community = new Community();

        createPost = new CreatePostRequest();
    }

    @Test
    public void testGetPost_Success() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Post result = postService.getPost(postId);

        assertEquals(post, result);
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    public void testGetPost_PostNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> postService.getPost(postId));

        assertEquals("Post not found", thrown.getMessage());
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    public void testCreatePost() {
        when(userService.getUserById(userId)).thenReturn(user);

        postService.createPost(createPost, userId);

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testCreatePostWithCommunity() {
        CreatePostRequest createRequest = new CreatePostRequest();

        when(userService.getUserById(userId)).thenReturn(user);
        when(communityService.getById(communityId)).thenReturn(community);

        postService.createPost(createRequest, userId, communityId);

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testDeletePost() {
        when(userService.getUserById(userId)).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId)).thenReturn(Optional.of(new UserCommunity(user, community, Role.ADMIN)));

        postService.deletePost(postId, userId);

        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void testDeletePostNotAllowed() {
        when(userService.getUserById(userId)).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId)).thenReturn(Optional.empty());

        User postOwner = new User();
        postOwner.setId(UUID.randomUUID());
        postOwner.setEmail("postOwner@gmail.com");

        post.setAuthor(postOwner);

        NotAllowedException exception = assertThrows(NotAllowedException.class, () -> {
            postService.deletePost(postId, userId);
        });

        assertEquals("You do not have permission to delete this post", exception.getMessage());
    }

    @Test
    void testToggleLike() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userService.getUserById(userId)).thenReturn(user);

        postService.toggleLike(postId, userId);

        verify(postLikeRepository, times(1)).save(any(PostLike.class));
    }

    @Test
    void testGetFeed() {
        User friend = new User();
        friend.setId(UUID.randomUUID());
        friend.setPosts(Set.of(post));

        when(userService.getFriends(userId)).thenReturn(List.of(friend));

        List<Post> feed = postService.getFeed(userId);

        assertEquals(1, feed.size());
        assertEquals(post, feed.get(0));
    }

    @Test
    void testAddComment() {
        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setAuthor(user);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.addComment(postId, comment);

        verify(commentRepository, times(1)).save(comment);
        assertEquals(post, comment.getPost());
    }

    @Test
    void testAddComment_PostNotFound() {
        Comment comment = new Comment();
        comment.setText("Test comment");

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            postService.addComment(postId, comment);
        });

        assertEquals("Post not found", exception.getMessage());
    }
}
