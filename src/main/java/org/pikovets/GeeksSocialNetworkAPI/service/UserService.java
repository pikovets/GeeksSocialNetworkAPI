package org.pikovets.GeeksSocialNetworkAPI.service;

import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.ChangeRoleRequest;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.UserCommunityDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.user.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.BadRequestException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
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
    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final TransactionalOperator transactionalOperator;

    @Autowired
    public UserService(UserRepository userRepository, UserRelationshipRepository userRelationshipRepository, UserCommunityRepository userCommunityRepository, CommunityRepository communityRepository, ProfileRepository profileRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, TransactionalOperator transactionalOperator) {
        this.userRepository = userRepository;
        this.userRelationshipRepository = userRelationshipRepository;
        this.userCommunityRepository = userCommunityRepository;
        this.communityRepository = communityRepository;
        this.profileRepository = profileRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.transactionalOperator = transactionalOperator;
    }

    public Flux<UserDTO> getAllUsers() {
        return userRepository.findAll().map(this::convertToUserDTO);
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

    public Mono<UserDTO> updateUser(Mono<User> updatedUserMono, UUID id) {
        return updatedUserMono.flatMap(updatedUser -> {
            updatedUser.setId(id);
            return userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).flatMap(existingUser -> {
                enrichUser(updatedUserMono, existingUser.getId());
                return userRepository.save(updatedUser)
                        .as(transactionalOperator::transactional)
                        .map(this::convertToUserDTO);
            });
        });
    }

    public Mono<Void> deleteUser(UUID id) {
        return profileRepository.deleteByUserId(id).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).then(userRepository.deleteById(id)).as(transactionalOperator::transactional);
    }

    public Flux<UserDTO> getUsersByName(String name) {
        return userRepository.findAll().filter(user -> (user.getFirstName() + user.getLastName()).contains(name)).map(this::convertToUserDTO);
    }

    public Flux<UserDTO> getFriends(UUID userId) {
        return getUserById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)))
                .thenMany(
                        userRelationshipRepository.getFriends(userId)
                                .switchIfEmpty(Flux.error(new NotFoundException("No friends found")))
                                .flatMap(rel -> {
                                    UUID friendId = rel.getRequesterId().equals(userId)
                                            ? rel.getAcceptorId()
                                            : rel.getRequesterId();
                                    return getUserById(friendId);
                                })
                );
    }

    public Flux<UserDTO> getAcceptFriendRequests(UUID userId) {
        return userRelationshipRepository.findByAcceptorIdAndType(userId, RelationshipType.ACCEPTOR_PENDING).flatMap(rel -> getUserById(rel.getAcceptorId()));
    }

    public Flux<CommunityDTO> getCommunities(UUID userId) {
        return userCommunityRepository.findByUserId(userId).flatMap(rel -> getCommunityById(rel.getCommunityId()));
    }

    public Mono<UserCommunityDTO> changeCommunityRole(UUID userId, Mono<ChangeRoleRequest> changeRoleRequestMono, UUID authUserId) {
        return changeRoleRequestMono.flatMap(changeRoleRequest -> userCommunityRepository.findByCommunityIdAndUserId(changeRoleRequest.getCommunityId(), authUserId)
                .switchIfEmpty(Mono.error(new NotFoundException(NOT_ADMIN)))
                .filter(authUserCommunity -> CommunityRole.ADMIN.equals(authUserCommunity.getUserRole()))
                .switchIfEmpty(Mono.error(new NotAllowedException(NOT_ADMIN)))
                .flatMap(admin -> userCommunityRepository.findByCommunityIdAndUserId(changeRoleRequest.getCommunityId(), userId))
                .switchIfEmpty(Mono.error(new NotFoundException(NOT_MEMBER)))
                .flatMap(userCommunity -> {
                    userCommunity.setUserRole(changeRoleRequest.getNewRole());
                    return userCommunityRepository.save(userCommunity);
                })
                .as(transactionalOperator::transactional)
                .map(this::convertToUserCommunityDTO));
    }

    public Mono<Void> validateEmailUnique(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> Mono.error(new BadRequestException("Email is already taken")))
                .then();
    }

    public Mono<Void> validatePassword(UUID userId, String rawPassword) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                        return Mono.error(new BadRequestException("Password mismatch"));
                    }
                    return Mono.empty();
                });
    }

    public Mono<UserDTO> enrichUser(Mono<User> expandableUserMono, UUID id) {
        return expandableUserMono.flatMap(expandableUser -> userRepository.findById(id).switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND))).map(userHelper -> {
            expandableUser.setIsActive(userHelper.getIsActive());
            expandableUser.setRole(userHelper.getRole());
            return expandableUser;
        }).map(this::convertToUserDTO));
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