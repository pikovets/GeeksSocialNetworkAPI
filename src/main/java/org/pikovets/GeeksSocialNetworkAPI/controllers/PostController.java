package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.comment.CommentDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.comment.CreateCommentRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.Comment;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommentRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.CommentService;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/posts")
@Tag(name = "Post")
public class PostController {
    private final PostService postService;
    private final CommentRepository commentRepository;
    private final IAuthenticationFacade authenticationFacade;

    @Autowired
    public PostController(PostService postService, CommunityRepository communityRepository, CommentRepository commentRepository, CommentService commentService, IAuthenticationFacade authenticationFacade) {
        this.postService = postService;
        this.commentRepository = commentRepository;
        this.authenticationFacade = authenticationFacade;
    }

    @Operation(
            summary = "Get post",
            description = "Get specific post by id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Post id",
                            required = true
                    ),
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}")
    public Mono<PostDTO> getPost(@PathVariable("id") UUID postId) {
        return postService.getPost(postId);
    }

    @Operation(
            summary = "Delete post",
            description = "Delete specific post by id. Also checks if the current user is owner of the post",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Post id",
                            required = true
                    ),
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Not Allowed",
                            responseCode = "405",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @DeleteMapping("/{id}")
    public Mono<Void> deletePost(@PathVariable("id") UUID postId) {
        return authenticationFacade.getUserID().flatMap(authUserId -> postService.deletePost(postId, authUserId)).then();
    }

    @Operation(
            summary = "Toggle like",
            description = "Like / dislike specific post by id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Post id",
                            required = true
                    ),
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @PostMapping("/{id}/toggleLike")
    public Mono<Void> toggleLike(@PathVariable("id") UUID postId) {
        return authenticationFacade.getUserID().flatMap(authUserId -> postService.toggleLike(postId, authUserId)).then();
    }

    @Operation(
            summary = "Get comments",
            description = "Get comments by post id",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Post id",
                            required = true
                    ),
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("{id}/comments")
    public Flux<CommentDTO> getCommentsByPostId(@PathVariable("id") String postId) {
        return commentRepository.findByPostId(postId).map(postService::convertToCommentDTO);
    }

    @Operation(
            summary = "Add comment",
            description = "Add a comment on a specific post",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Post id",
                            required = true
                    ),
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @PostMapping("/{id}/addComment")
    public Mono<Void> addComment(@PathVariable("id") UUID postId, @RequestBody Mono<CreateCommentRequest> commentRequest) {
        return authenticationFacade.getUserID().flatMap(authUserId -> postService.addComment(postId, authUserId, commentRequest)).then();
    }

    @Operation(
            summary = "Get feed",
            description = "Get feed",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403",
                            content = @Content
                    )
            }
    )
    @GetMapping("/feed")
    public Flux<PostDTO> getFeed() {
        return authenticationFacade.getUserID().flatMapMany(postService::getFeed);
    }
}
