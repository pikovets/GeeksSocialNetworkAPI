package org.pikovets.GeeksSocialNetworkAPI.service;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityProfileDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityUpdateRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CreateCommunityRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.JoinType;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class CommunityService {
    private static final String COMMUNITY_NOT_FOUND = "Community not found";
    private static final String USER_NOT_FOUND_IN_COMMUNITY = "User not found in community";
    private static final String USER_NOT_ALLOWED = "User not allowed to perform this action";

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final UserCommunityRepository userCommunityRepository;
    private final ModelMapper modelMapper;
    private final TransactionalOperator transactionalOperator;

    @Autowired
    public CommunityService(UserRepository userRepository, CommunityRepository communityRepository, UserCommunityRepository userCommunityRepository, UserService userService, ModelMapper modelMapper, TransactionalOperator transactionalOperator) {
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.userCommunityRepository = userCommunityRepository;
        this.modelMapper = modelMapper;
        this.transactionalOperator = transactionalOperator;
    }

    public Flux<CommunityDTO> getAll() {
        return communityRepository.findAll().map(this::convertToCommunityDTO);
    }

    public Mono<CommunityDTO> getById(UUID communityId) {
        return communityRepository.findById(communityId).switchIfEmpty(Mono.error(new NotFoundException(COMMUNITY_NOT_FOUND))).map(this::convertToCommunityDTO);
    }

    public Mono<CommunityProfileDTO> getProfileById(UUID communityId) {
        return communityRepository.findById(communityId).switchIfEmpty(Mono.error(new NotFoundException(COMMUNITY_NOT_FOUND))).map(this::convertToCommunityProfileDTO);
    }

    public Mono<CommunityDTO> createCommunity(Mono<CreateCommunityRequest> communityRequestMono, UUID adminId) {
        return communityRequestMono.flatMap(communityRequest -> {
            Community toSave = new Community();

            toSave.setName(communityRequest.getName());
            toSave.setCategory(communityRequest.getCategory());
            toSave.setJoinType(communityRequest.getJoinType());
            //      toSave.setPublishPermission(communityRequest.getPublishPermission());

            return communityRepository
                    .save(toSave)
                    .flatMap(savedCommunity ->
                            userRepository
                                    .findById(adminId)
                                    .switchIfEmpty(
                                            Mono.error(new NotFoundException("User not found"))
                                    )
                                    .flatMap(adminUser -> {
                                                System.out.println(adminUser.getId() + " " + savedCommunity.getId());
                                                return userCommunityRepository
                                                        .save(new UserCommunity(adminUser.getId(),
                                                                savedCommunity.getId(),
                                                                CommunityRole.ADMIN));
                                            }
                                    )
                                    .thenReturn(savedCommunity)
                    )
                    .map(this::convertToCommunityDTO);
        }).as(transactionalOperator::transactional);
    }

    public Mono<Void> deleteCommunityById(UUID communityId, UUID authUserId) {
        return userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId)
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)))
                .filter(userCommunity -> userCommunity.getUserRole().equals(CommunityRole.ADMIN))
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)))
                .then(userCommunityRepository.deleteByCommunityIdAndUserId(communityId, authUserId))
                .then(communityRepository.deleteById(communityId))
                .as(transactionalOperator::transactional);
    }

    public Mono<UserCommunity> addMember(UUID communityId, UUID authUserId) {
        return userCommunityRepository.save(new UserCommunity(authUserId, communityId, CommunityRole.MEMBER))
                .as(transactionalOperator::transactional);
    }

    public Mono<UserCommunity> joinCommunity(UUID communityId, UUID userId) {
        return communityRepository.findById(communityId).switchIfEmpty(Mono.error(new NotAllowedException(COMMUNITY_NOT_FOUND))).flatMap(community -> community.getJoinType().equals(JoinType.OPEN) ? addMember(communityId, userId) : sendJoinCommunityRequest(communityId, userId));
    }

    public Mono<UserCommunity> sendJoinCommunityRequest(UUID communityId, UUID userId) {
        return userCommunityRepository.save(new UserCommunity(userId, communityId, CommunityRole.WAITING_TO_ACCEPT))
                .as(transactionalOperator::transactional);
    }

    public Mono<Void> deleteJoinCommunityRequest(UUID communityId, UUID userId, UUID authUserId) {
        return userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId)
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_FOUND_IN_COMMUNITY)))
                .filter(userCommunity -> userCommunity.getUserRole().equals(CommunityRole.ADMIN) || userCommunity.getUserRole().equals(CommunityRole.MODERATOR))
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)))
                .then(userCommunityRepository.deleteByCommunityIdAndUserId(communityId, userId))
                .as(transactionalOperator::transactional)
                .then();
    }

    public Mono<Void> leaveCommunity(UUID communityId, UUID authUserId) {
        return userCommunityRepository.deleteByCommunityIdAndUserId(communityId, authUserId)
                .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND_IN_COMMUNITY)))
                .as(transactionalOperator::transactional)
                .then();
    }

    public Mono<CommunityDTO> updateCommunity(Mono<CommunityUpdateRequest> communityUpdateDTOMono, UUID communityId, UUID userId) {
        return communityUpdateDTOMono.flatMap(communityUpdateRequest -> getCurrentUserRole(communityId, userId)
                .filter(role -> role == CommunityRole.ADMIN || role == CommunityRole.MODERATOR)
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)))
                .then(communityRepository.findById(communityId)
                        .switchIfEmpty(Mono.error(new NotFoundException(COMMUNITY_NOT_FOUND))))
                .flatMap(current -> {
                    if (communityUpdateRequest.getName() != null && !communityUpdateRequest.getName().isEmpty()) {
                        current.setName(communityUpdateRequest.getName());
                    }
                    if (communityUpdateRequest.getCategory() != null) {
                        current.setCategory(communityUpdateRequest.getCategory());
                    }
                    if (communityUpdateRequest.getDescription() != null && !communityUpdateRequest.getDescription().isEmpty()) {
                        current.setDescription(communityUpdateRequest.getDescription());
                    }
                    if (communityUpdateRequest.getPhotoLink() != null && !communityUpdateRequest.getPhotoLink().isEmpty()) {
                        current.setPhotoLink(communityUpdateRequest.getPhotoLink());
                    }
                    if (communityUpdateRequest.getPublishPermission() != null) {
                        current.setPublishPermission(communityUpdateRequest.getPublishPermission());
                    }
                    if (communityUpdateRequest.getJoinType() != null) {
                        current.setJoinType(communityUpdateRequest.getJoinType());
                    }
                    return communityRepository.save(current);
                }).as(transactionalOperator::transactional)
                .map(this::convertToCommunityDTO));
    }

    public Flux<CommunityDTO> searchCommunityByName(String name) {
        return communityRepository.findByNameContainingIgnoreCase(name).map(this::convertToCommunityDTO);
    }

    public Flux<UserDTO> getCommunityJoinRequests(UUID communityId, UUID userId) {
        return getCurrentUserRole(communityId, userId)
                .filter(role -> role == CommunityRole.ADMIN || role == CommunityRole.MODERATOR)
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)))
                .flatMapMany(role ->
                        userCommunityRepository.findByCommunityId(communityId)
                                .filter(request -> request.getUserRole().equals(CommunityRole.WAITING_TO_ACCEPT))
                                .flatMap(userCommunity -> userRepository.findById(userCommunity.getUserId()))
                                .map(this::convertToUserDTO)
                );
    }

    public Mono<CommunityRole> getCurrentUserRole(UUID communityId, UUID userId) {
        return userCommunityRepository
                .findByCommunityIdAndUserId(communityId, userId)
                .map(UserCommunity::getUserRole)
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)));
    }

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public CommunityDTO convertToCommunityDTO(Community community) {
        return modelMapper.map(community, CommunityDTO.class);
    }

    public CommunityProfileDTO convertToCommunityProfileDTO(Community community) {
        return modelMapper.map(community, CommunityProfileDTO.class);
    }

}
