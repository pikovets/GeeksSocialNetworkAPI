package org.pikovets.GeeksSocialNetworkAPI.controllers;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship.UserRelationshipDTO;
import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.pikovets.GeeksSocialNetworkAPI.security.IAuthenticationFacade;
import org.pikovets.GeeksSocialNetworkAPI.service.UserRelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/userRelations")
public class UserRelationshipController {
    private final UserRelationshipService userRelationshipService;
    private final ModelMapper modelMapper;
    private final IAuthenticationFacade authenticationFacade;

    @Autowired
    public UserRelationshipController(UserRelationshipService userRelationshipService, ModelMapper modelMapper, IAuthenticationFacade authenticationFacade) {
        this.userRelationshipService = userRelationshipService;
        this.modelMapper = modelMapper;
        this.authenticationFacade = authenticationFacade;
    }

    @GetMapping("/getFriendRequest")
    public ResponseEntity<UserRelationshipDTO> getFriendRequest(@RequestParam("userId") UUID userId) {
        return new ResponseEntity<>(convertToUserRelationshipDTO(userRelationshipService.getFriendRequest(userId, authenticationFacade.getUserID())), HttpStatus.OK);
    }

    @PostMapping("/sendFriendRequest")
    public ResponseEntity<HttpStatus> sendFriendRequest(@RequestParam("userId") UUID userId) {
        userRelationshipService.sendFriendRequest(userId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/acceptFriendRequest")
    public ResponseEntity<HttpStatus> acceptFriendRequest(@RequestParam("userId") UUID userId) {
        userRelationshipService.acceptFriendRequest(userId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/removeFriendRequest")
    public ResponseEntity<HttpStatus> removeFriendRequest(@RequestParam("userId") UUID userId) {
        userRelationshipService.removeFriendRequest(userId, authenticationFacade.getUserID());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public UserRelationshipDTO convertToUserRelationshipDTO(UserRelationship userRelationship) {
        return modelMapper.map(userRelationship, UserRelationshipDTO.class);
    }
}
