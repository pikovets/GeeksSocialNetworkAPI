package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostResponse;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.Comment;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/posts")
@Tag(name = "Post")
public class PostController {
    private final PostService postService;
    private final IAuthenticationFacade authenticationFacade;
    private final ModelMapper modelMapper;

    @Autowired
    public PostController(PostService postService, IAuthenticationFacade authenticationFacade, ModelMapper modelMapper) {
        this.postService = postService;
        this.authenticationFacade = authenticationFacade;
        this.modelMapper = modelMapper;
    }

    @Operation(
            summary = "Get specific post",
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
    public ResponseEntity<PostDTO> getPost(@PathVariable("id") UUID postId) {
        return new ResponseEntity<>(convertToPostDTO(postService.getPost(postId)), HttpStatus.OK);
    }

    @Operation(
            summary = "Delete specific post",
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
    public ResponseEntity<HttpStatus> deletePost(@PathVariable("id") UUID postId) {
        postService.deletePost(postId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
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
    public ResponseEntity<HttpStatus> toggleLike(@PathVariable("id") UUID postId) {
        postService.toggleLike(postId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
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
    public ResponseEntity<HttpStatus> addComment(@PathVariable("id") UUID postId, @RequestBody Comment comment) {
        postService.addComment(postId, comment);
        return new ResponseEntity<>(HttpStatus.OK);
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
    public ResponseEntity<PostResponse> getFeed() {
        return new ResponseEntity<>(new PostResponse(postService.getFeed(authenticationFacade.getUserID()).stream().map(this::convertToPostDTO).toList()), HttpStatus.OK);
    }

    public PostDTO convertToPostDTO(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }
}
