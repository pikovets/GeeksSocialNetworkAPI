package org.pikovets.GeeksSocialNetworkAPI.service;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.*;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserResponse;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CommunityService {
    private static final String COMMUNITY_NOT_FOUND = "Community not found";
    private static final String USER_NOT_FOUND_IN_COMMUNITY = "User not found in community";
    private static final String USER_NOT_ALLOWED = "User not allowed to perform this action";

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final UserCommunityRepository userCommunityRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CommunityService(UserRepository userRepository, CommunityRepository communityRepository, UserCommunityRepository userCommunityRepository, UserService userService, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.userCommunityRepository = userCommunityRepository;
        this.modelMapper = modelMapper;
    }

    public CommunityResponse getAll() {
        return new CommunityResponse(communityRepository.findAll().map(this::convertToCommunityDTO));
    }

    public Mono<CommunityDTO> getById(UUID communityId) {
        return communityRepository.findById(communityId).switchIfEmpty(Mono.error(new NotFoundException(COMMUNITY_NOT_FOUND))).map(this::convertToCommunityDTO);
    }

    public Mono<CommunityProfileDTO> getProfileById(UUID communityId) {
        return communityRepository.findById(communityId).switchIfEmpty(Mono.error(new NotFoundException(COMMUNITY_NOT_FOUND))).map(this::convertToCommunityProfileDTO);
    }

    public Mono<CommunityDTO> createCommunity(CreateCommunityRequest communityRequest, UUID adminId) {
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
                                .flatMap(adminUser ->
                                        userCommunityRepository
                                                .save(new UserCommunity(adminUser.getId(),
                                                        savedCommunity.getId(),
                                                        CommunityRole.ADMIN))
                                )
                                .thenReturn(savedCommunity)
                )
                .map(this::convertToCommunityDTO);
    }

    public Mono<Void> deleteCommunityById(UUID communityId, UUID authUserId) {
        userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId).switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED))).filter(userCommunity -> userCommunity.getUserRole().equals(CommunityRole.ADMIN)).switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)));
        return communityRepository.deleteById(communityId);
    }

    public Mono<UserCommunity> addMember(UUID communityId, UUID authUserId) {
        return userCommunityRepository.save(new UserCommunity(authUserId, communityId, CommunityRole.MEMBER));
    }

    public Mono<UserCommunity> sendJoinCommunityRequest(UUID communityId, UUID userId) {
        return userCommunityRepository.save(new UserCommunity(userId, communityId, CommunityRole.WAITING_TO_ACCEPT));
    }

    public Flux<Void> deleteJoinCommunityRequest(UUID communityId, UUID userId, UUID authUserId) {
        userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId).switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_FOUND_IN_COMMUNITY))).filter(userCommunity -> userCommunity.getUserRole().equals(CommunityRole.ADMIN) || userCommunity.getUserRole().equals(CommunityRole.MODERATOR)).switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)));
        return userCommunityRepository.deleteByCommunityIdAndUserId(communityId, userId);
    }

    public Flux<Void> leaveCommunity(UUID communityId, UUID authUserId) {
        return userCommunityRepository.deleteByCommunityIdAndUserId(communityId, authUserId).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND_IN_COMMUNITY)));
    }

    public Mono<CommunityDTO> updateCommunity(CommunityUpdateDTO dto, UUID communityId, UUID userId) {
        return getCurrentUserRole(communityId, userId).getRole()
                .filter(role -> role == CommunityRole.ADMIN || role == CommunityRole.MODERATOR)
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED)))
                .then(communityRepository.findById(communityId)
                        .switchIfEmpty(Mono.error(new NotFoundException(COMMUNITY_NOT_FOUND))))
                .flatMap(current -> {
                    if (dto.getName() != null && !dto.getName().isEmpty()) {
                        current.setName(dto.getName());
                    }
                    if (dto.getCategory() != null) {
                        current.setCategory(dto.getCategory());
                    }
                    if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
                        current.setDescription(dto.getDescription());
                    }
                    if (dto.getPhotoLink() != null && !dto.getPhotoLink().isEmpty()) {
                        current.setPhotoLink(dto.getPhotoLink());
                    }
                    if (dto.getPublishPermission() != null) {
                        current.setPublishPermission(dto.getPublishPermission());
                    }
                    if (dto.getJoinType() != null) {
                        current.setJoinType(dto.getJoinType());
                    }
                    return communityRepository.save(current);
                }).map(this::convertToCommunityDTO);
    }


    public CommunityResponse searchCommunityByName(String name) {
        return new CommunityResponse(communityRepository.findByNameContainingIgnoreCase(name).map(this::convertToCommunityDTO));
    }

    public UserResponse getCommunityJoinRequests(UUID communityId) {
        return new UserResponse(userCommunityRepository.findByCommunityId(communityId)
                .filter(request -> request.getUserRole().equals(CommunityRole.WAITING_TO_ACCEPT))
                .flatMap(userCommunity -> userRepository.findById(userCommunity.getId().getUserId()))
                .map(this::convertToUserDTO));
    }

    public CommunityRoleResponse getCurrentUserRole(UUID communityId, UUID userId) {
        return new CommunityRoleResponse(userCommunityRepository
                .findByCommunityIdAndUserId(communityId, userId)
                .map(UserCommunity::getUserRole)
                .switchIfEmpty(Mono.error(new NotAllowedException(USER_NOT_ALLOWED))));
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
