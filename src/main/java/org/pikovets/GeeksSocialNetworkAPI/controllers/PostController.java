package org.pikovets.GeeksSocialNetworkAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.core.ErrorUtils;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.UpdatePostRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.ErrorObject;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
@Tag(name = "Post")
public class PostController {
    private final ModelMapper modelMapper;
    private final PostService postService;

    @Autowired
    public PostController(ModelMapper modelMapper, PostService postService) {
        this.modelMapper = modelMapper;
        this.postService = postService;
    }

    @Operation(
            summary = "Get user by ID",
            description = "Returns a single post",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of post to return"
                    )
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
                            responseCode = "403"
                    )
            }
    )
    @GetMapping("/{id}")
    public PostDTO getPostById(@PathVariable("id") UUID id) {
        return convertToPostDTO(postService.getPostById(id));
    }

    @Operation(
            summary = "Update post by ID",
            description = "Updates a specific post by id. If the post text is longer than 2200, a Bad Request error will be thrown",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of post to update"
                    )
            },
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Bad Request",
                            responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Not Found",
                            responseCode = "404",
                            content = @Content(schema = @Schema(implementation = ErrorObject.class))
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<HttpStatus> updatePost(@PathVariable("id") UUID id, @RequestBody @Valid UpdatePostRequest updateRequest,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ErrorUtils.returnBadRequestException(bindingResult);
        }

        postService.updatePost(id, updateRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public PostDTO convertToPostDTO(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }

    public Post convertToPost(PostDTO postDTO) {
        return modelMapper.map(postDTO, Post.class);
    }
}