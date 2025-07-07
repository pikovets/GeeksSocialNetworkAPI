package org.pikovets.GeeksSocialNetworkAPI.service;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.profile.ProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship.UserRelationshipDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Profile;
import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserRelationshipService {
    private final String USER_RELATIONSHIP_NOT_FOUND = "User relationship not found";

    private final UserRelationshipRepository userRelationshipRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public UserRelationshipService(UserRelationshipRepository userRelationshipRepository, ModelMapper modelMapper) {
        this.userRelationshipRepository = userRelationshipRepository;
        this.modelMapper = modelMapper;
    }

    public Mono<UserRelationshipDTO> getFriendRequest(UUID userId, UUID authUserId) {
        return userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId).switchIfEmpty(Mono.error(new NotFoundException(USER_RELATIONSHIP_NOT_FOUND))).map(this::convertToUserRelationshipDTO);
    }

    @Transactional
    public Mono<UserRelationshipDTO> sendFriendRequest(UUID userId, UUID authUserId) {
        return userRelationshipRepository.save(new UserRelationship(new UserRelationship.UserRelationshipId(authUserId, userId), RelationshipType.ACCEPTOR_PENDING)).map(this::convertToUserRelationshipDTO);
    }

    @Transactional
    public Mono<UserRelationshipDTO> acceptFriendRequest(UUID userId, UUID authUserId) {
        return userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId).flatMap(relation -> {
            relation.setType(RelationshipType.FRIENDS);
            return userRelationshipRepository.save(relation);
        }).map(this::convertToUserRelationshipDTO);
    }

    @Transactional
    public Mono<Void> removeFriendRequest(UUID firstUserId, UUID secondUserId) {
        return userRelationshipRepository.deleteByRequesterIdAndAcceptorId(firstUserId, secondUserId);
    }

    public UserRelationshipDTO convertToUserRelationshipDTO(UserRelationship userRelationship) {
        return modelMapper.map(userRelationship, UserRelationshipDTO.class);
    }
}
