package org.pikovets.GeeksSocialNetworkAPI.service;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.user_relationship.UserRelationshipDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserRelationshipService {
    private final String USER_RELATIONSHIP_NOT_FOUND = "User relationship not found";

    private final UserRelationshipRepository userRelationshipRepository;
    private final ModelMapper modelMapper;
    private final TransactionalOperator transactionalOperator;

    @Autowired
    public UserRelationshipService(UserRelationshipRepository userRelationshipRepository, ModelMapper modelMapper, TransactionalOperator transactionalOperator) {
        this.userRelationshipRepository = userRelationshipRepository;
        this.modelMapper = modelMapper;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<UserRelationshipDTO> getFriendRequest(UUID userId, UUID authUserId) {
        return userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId).switchIfEmpty(Mono.error(new NotFoundException(USER_RELATIONSHIP_NOT_FOUND))).map(this::convertToUserRelationshipDTO);
    }

    public Mono<UserRelationshipDTO> sendFriendRequest(UUID userId, UUID authUserId) {
        return userRelationshipRepository.save(new UserRelationship(authUserId, userId, RelationshipType.ACCEPTOR_PENDING))
                .as(transactionalOperator::transactional).map(this::convertToUserRelationshipDTO);
    }

    public Mono<Void> acceptFriendRequest(UUID userId, UUID authUserId) {
        return userRelationshipRepository.findByRequesterIdAndAcceptorId(userId, authUserId).flatMap(relation -> {
                    return userRelationshipRepository.updateRelationshipType(RelationshipType.FRIENDS.toString(), relation.getRequesterId(), relation.getAcceptorId());
                }).as(transactionalOperator::transactional);
    }

    public Mono<Void> removeFriendRequest(UUID firstUserId, UUID secondUserId) {
        return userRelationshipRepository.deleteByRequesterIdAndAcceptorId(firstUserId, secondUserId)
                .as(transactionalOperator::transactional);
    }

    public UserRelationshipDTO convertToUserRelationshipDTO(UserRelationship userRelationship) {
        return modelMapper.map(userRelationship, UserRelationshipDTO.class);
    }
}
