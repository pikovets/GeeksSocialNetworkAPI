package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRelationshipRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserRelationshipService {

    private final UserRelationshipRepository userRelationshipRepository;
    private final UserService userService;

    @Autowired
    public UserRelationshipService(UserRelationshipRepository userRelationshipRepository, UserService userService) {
        this.userRelationshipRepository = userRelationshipRepository;
        this.userService = userService;
    }

    public UserRelationship getFriendRequest(UUID userId, UUID authUserId) {
        return userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId).orElseThrow(new NotFoundException("User relationship not found"));
    }

    @Transactional
    public void sendFriendRequest(UUID userId, UUID authUserId) {
        userRelationshipRepository.save(new UserRelationship(userService.getUserById(authUserId), userService.getUserById(userId), RelationshipType.ACCEPTOR_PENDING));
    }

    @Transactional
    public void acceptFriendRequest(UUID userId, UUID authUserId) {
        UserRelationship userRelationship = getFriendRequest(userId, authUserId);

        if (authUserId.equals(userRelationship.getRequester().getId())) {
            throw new BadRequestException("Requester cannot accept the request");
        }

        userRelationship.setType(RelationshipType.FRIENDS);
        userRelationshipRepository.save(userRelationship);
    }

    @Transactional
    public void removeFriendRequest(UUID firstUserId, UUID secondUserId) {
        userRelationshipRepository.delete(getFriendRequest(firstUserId, secondUserId));
    }
}
