package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.security.AuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
@Tag(name = "Comment")
public class CommentController {
    private final AuthenticationFacade authenticationFacade;
    private final CommentService commentService;

    @Autowired
    public CommentController(AuthenticationFacade authenticationFacade, CommentService commentService) {
        this.authenticationFacade = authenticationFacade;
        this.commentService = commentService;
    }

    @Operation(
            summary = "Toggle like on comment",
            description = "Toggle the like status for a specific comment by comment id",
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
    public Mono<Void> toggleCommentLike(@PathVariable("id") UUID commentId) {
        return authenticationFacade.getUserID().flatMap(authUserId -> commentService.toggleCommentLike(commentId, authUserId));
    }

    @Operation(
            summary = "Delete comment",
            description = "Delete Comment by comment id",
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
    public Mono<Void> deleteComment(@PathVariable("id") UUID commentId) {
        return authenticationFacade.getUserID().flatMap(authUserId -> commentService.deleteComment(commentId, authUserId));
    }
}
