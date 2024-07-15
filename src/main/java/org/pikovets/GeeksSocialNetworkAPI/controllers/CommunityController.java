package org.pikovets.GeeksSocialNetworkAPI.controllers;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CreateCommunityRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.post.PostDTO;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.security.AuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/communities")
public class CommunityController {
    private final AuthenticationFacade authenticationFacade;
    private final CommunityService communityService;
    private final ModelMapper modelMapper;

    @Autowired
    public CommunityController(AuthenticationFacade authenticationFacade, CommunityService communityService, ModelMapper modelMapper) {
        this.authenticationFacade = authenticationFacade;
        this.communityService = communityService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<CommunityResponse> getAllCommunities() {
        return new ResponseEntity<>(new CommunityResponse(communityService.getAll().stream().map(this::convertToCommunityDTO).toList()), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityDTO> getCommunity(@PathVariable("id") UUID communityId) {
        return new ResponseEntity<>(convertToCommunityDTO(communityService.getById(communityId)), HttpStatus.OK);
    }

    @GetMapping("/getCommunityProfile/{id}")
    public ResponseEntity<CommunityProfileDTO> getCommunityProfile(@PathVariable("id") UUID communityId) {
        return new ResponseEntity<>(convertToCommunityProfileDTO(communityService.getById(communityId)), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> createCommunity(@RequestBody CreateCommunityRequest communityRequest) {
        communityService.createCommunity(communityRequest, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCommunity(@PathVariable("id") UUID id) {
        communityService.deleteCommunityById(id, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<HttpStatus> joinCommunity(@PathVariable("id") UUID communityId) {
        communityService.addMember(communityId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<HttpStatus> leaveCommunity(@PathVariable("id") UUID communityId) {
        communityService.leaveCommunity(communityId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/searchByName")
    public ResponseEntity<CommunityResponse> searchCommunityByName(@RequestParam(name = "name") String communityName) {
        return new ResponseEntity<>(new CommunityResponse(communityService.searchCommunityByName(communityName).stream().map(this::convertToCommunityDTO).toList()), HttpStatus.OK);
    }

    @GetMapping("/{communityId}/getCurrentUserRole")
    public ResponseEntity<Role> getCurrentUserRole(@PathVariable("communityId") UUID communityId) {
        return new ResponseEntity<>(communityService.getCurrentUserRole(communityId, authenticationFacade.getUserID()), HttpStatus.OK);
    }

    public CommunityDTO convertToCommunityDTO(Community community) {
        return modelMapper.map(community, CommunityDTO.class);
    }


    public CommunityProfileDTO convertToCommunityProfileDTO(Community community) {
        CommunityProfileDTO communityProfileDTO = new CommunityProfileDTO();
        communityProfileDTO.setId(community.getId());
        communityProfileDTO.setName(community.getName());
        communityProfileDTO.setDescription(community.getDescription());
        communityProfileDTO.setCategory(community.getCategory());
        communityProfileDTO.setPhotoLink(community.getPhotoLink());
        communityProfileDTO.setPublishPermission(community.getPublishPermission());
        communityProfileDTO.setJoinType(community.getJoinType());
        communityProfileDTO.setPosts(mapPostsToPostDTOs(community.getPosts()));
        return communityProfileDTO;
    }

    private Set<PostDTO> mapPostsToPostDTOs(Set<Post> posts) {
        return posts.stream()
                .map(post -> modelMapper.map(post, PostDTO.class))
                .collect(Collectors.toSet());
    }
}