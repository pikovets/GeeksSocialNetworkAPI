package org.pikovets.GeeksSocialNetworkAPI.service;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.ChangeRoleRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.UserCommunityDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserResponse;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRelationshipRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserService {
    private static final String USER_NOT_FOUND = "User not found";
    private static final String COMMUNITY_NOT_FOUND = "Community not found";
    private static final String NOT_ADMIN = "Authenticated user isn't an administrator of specified group";
    private static final String NOT_MEMBER = "User isn't a member of this group";

    private final UserRepository userRepository;
    private final UserCommunityRepository userCommunityRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final CommunityRepository communityRepository;
    private final ModelMapper modelMapper;


    @Autowired
    public UserService(UserRepository userRepository, UserRelationshipRepository userRelationshipRepository, UserCommunityRepository userCommunityRepository, CommunityRepository communityRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userRelationshipRepository = userRelationshipRepository;
        this.userCommunityRepository = userCommunityRepository;
        this.communityRepository = communityRepository;
        this.modelMapper = modelMapper;

    }

    public UserResponse getAllUsers() {
        return new UserResponse(userRepository.findAll().map(this::convertToUserDTO));
    }

    public Mono<UserDTO> getUserById(UUID id) {
        return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).map(this::convertToUserDTO);
    }

    public Mono<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)));
    }

    public Mono<CommunityDTO> getCommunityById(UUID id) {
        return communityRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException(COMMUNITY_NOT_FOUND))).map(this::convertToCommunityDTO);
    }


    public Mono<UserDTO> updateUser(User updatedUser, UUID id) {
        updatedUser.setId(id);
        return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).flatMap(existingUser -> {
            enrichUser(updatedUser, existingUser.getId());
            return userRepository.save(updatedUser);
        }).map(this::convertToUserDTO);
    }

    public Mono<Void> deleteUser(UUID id) {
        return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).flatMap(userRepository::delete);
    }

    public UserResponse getUsersByName(String name) {
        return new UserResponse(userRepository.findAll().filter(user -> (user.getFirstName() + user.getLastName()).contains(name)).map(this::convertToUserDTO));
    }

    public UserResponse getFriends(UUID userId) {
        return new UserResponse(userRelationshipRepository.getFriends(userId).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).flatMap(rel -> {
            if (rel.getId().getRequesterId().equals(userId)) {
                return getUserById(rel.getId().getAcceptorId());
            } else {
                return getUserById(rel.getId().getRequesterId());
            }
        }));
    }

    public UserResponse getAcceptFriendRequests(UUID userId) {
        return new UserResponse(userRelationshipRepository.findByAcceptorIdAndType(userId, RelationshipType.ACCEPTOR_PENDING).flatMap(rel -> getUserById(rel.getId().getAcceptorId())));
    }

    public CommunityResponse getCommunities(UUID userId) {
        return new CommunityResponse(userCommunityRepository.findByUserId(userId).flatMap(rel -> getCommunityById(rel.getId().getCommunityId())));
    }

    public Mono<UserCommunityDTO> changeCommunityRole(UUID userId, ChangeRoleRequest changeRoleRequest, UUID authUserId) {
        return userCommunityRepository.findByCommunityIdAndUserId(changeRoleRequest.getCommunityId(), authUserId)
                .switchIfEmpty(Mono.error(new NotFoundException(NOT_ADMIN)))
                .filter(authUserCommunity -> CommunityRole.ADMIN.equals(authUserCommunity.getUserRole()))
                .switchIfEmpty(Mono.error(new NotAllowedException(NOT_ADMIN)))
                .flatMap(admin -> userCommunityRepository.findByCommunityIdAndUserId(changeRoleRequest.getCommunityId(), userId))
                .switchIfEmpty(Mono.error(new NotFoundException(NOT_MEMBER)))
                .flatMap(userCommunity -> {
                    userCommunity.setUserRole(changeRoleRequest.getNewRole());
                    return userCommunityRepository.save(userCommunity);
                }).map(this::convertToUserCommunityDTO);
    }

    public Mono<UserDTO> enrichUser(User expandableUser, UUID id) {
        return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).map(userHelper -> {
            expandableUser.setIsActive(userHelper.getIsActive());
            expandableUser.setRole(userHelper.getRole());
            return expandableUser;
        }).map(this::convertToUserDTO);
    }

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public CommunityDTO convertToCommunityDTO(Community community) {
        return modelMapper.map(community, CommunityDTO.class);
    }

    public UserCommunityDTO convertToUserCommunityDTO(UserCommunity userCommunity) {
        return modelMapper.map(userCommunity, UserCommunityDTO.class);
    }
}