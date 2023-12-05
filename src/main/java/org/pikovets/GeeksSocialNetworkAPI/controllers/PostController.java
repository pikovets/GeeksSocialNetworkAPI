package org.pikovets.GeeksSocialNetworkAPI.controllers;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostController {
    private ModelMapper modelMapper;
    private PostService postService;

    @Autowired
    public PostController(ModelMapper modelMapper, PostService postService) {
        this.modelMapper = modelMapper;
        this.postService = postService;
    }

    public PostDTO convertToPostDTO(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }

    public Post convertToPost(PostDTO postDTO) {
        return modelMapper.map(postDTO, Post.class);
    }
}