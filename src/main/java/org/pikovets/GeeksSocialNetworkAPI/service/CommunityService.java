package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.community.CreateCommunityRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final UserCommunityRepository userCommunityRepository;
    private final UserService userService;

    @Autowired
    public CommunityService(CommunityRepository communityRepository, UserCommunityRepository userCommunityRepository, UserService userService) {
        this.communityRepository = communityRepository;
        this.userCommunityRepository = userCommunityRepository;
        this.userService = userService;
    }

    public List<Community> getAll() {
        return communityRepository.findAll();
    }

    public Community getById(UUID communityId) {
        return communityRepository.findById(communityId).orElseThrow(new NotFoundException("Community not found"));
    }

    @Transactional
    public void createCommunity(CreateCommunityRequest communityRequest, UUID adminId) {
        Community community = new Community();

        community.setName(communityRequest.getName());
        community.setCategory(communityRequest.getCategory());
//        community.setPublishPermission(communityRequest.getPublishPermission());
        community.setJoinType(communityRequest.getJoinType());

        communityRepository.save(community);

        User admin = userService.getUserById(adminId);
        userCommunityRepository.save(new UserCommunity(admin, community, Role.ADMIN));
    }

    @Transactional
    public void deleteCommunityById(UUID communityId, UUID authUserId) {
        Optional<UserCommunity> userCommunity = userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId);

        if (userCommunity.isEmpty() || !userCommunity.get().getUserRole().equals(Role.ADMIN)) {
            throw new NotAllowedException("User is not allowed to perform this action");
        }

        userCommunityRepository.delete(userCommunity.get());
        communityRepository.deleteById(communityId);
    }

    @Transactional
    public void addMember(UUID communityId, UUID authUserId) {
        userCommunityRepository.save(new UserCommunity(userService.getUserById(authUserId), communityRepository.findById(communityId).orElseThrow(new NotFoundException("Community not found")), Role.USER));
    }

    @Transactional
    public void leaveCommunity(UUID communityId, UUID authUserId) {
        userCommunityRepository.delete(userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId).orElseThrow(new NotFoundException("User not found in community")));
    }

    public List<Community> searchCommunityByName(String name) {
        return communityRepository.findByNameIgnoreCase(name);
    }

    public Role getCurrentUserRole(UUID communityId, UUID authUserId) {
        return userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId).map(UserCommunity::getUserRole).orElseThrow(new NotFoundException("User not found in community"));
    }

    public Set<User> getFollowers(UUID communityId) {
        return userCommunityRepository.findByCommunityId(communityId).stream().map(UserCommunity::getUser).collect(Collectors.toSet());
    }
}
