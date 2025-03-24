package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityUpdateDTO;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CreateCommunityRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotAllowedException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.CommunityRole;
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

    private static final String USER_NOT_FOUND_IN_COMMUNITY = "User not found in community";
    private static final String USER_NOT_ALLOWED = "User not allowed to perform this action";

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
        userCommunityRepository.save(new UserCommunity(admin, community, CommunityRole.ADMIN));
    }

    @Transactional
    public void deleteCommunityById(UUID communityId, UUID authUserId) {
        Optional<UserCommunity> userCommunity = userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId);

        if (userCommunity.isEmpty() || !userCommunity.get().getUserRole().equals(CommunityRole.ADMIN)) {
            throw new NotAllowedException(USER_NOT_ALLOWED);
        }

        userCommunityRepository.delete(userCommunity.get());
        communityRepository.deleteById(communityId);
    }

    @Transactional
    public void addMember(UUID communityId, UUID authUserId) {
        userCommunityRepository.save(new UserCommunity(userService.getUserById(authUserId), getById(communityId), CommunityRole.MEMBER));
    }

    @Transactional
    public void sendJoinCommunityRequest(UUID communityId, UUID userId) {
        userCommunityRepository.save(new UserCommunity(userService.getUserById(userId), getById(communityId), CommunityRole.WAITING_TO_ACCEPT));
    }

    @Transactional
    public void deleteJoinCommunityRequest(UUID communityId, UUID userId, UUID authUserId) {
        CommunityRole authUserRole = getCurrentUserRole(communityId, authUserId);
        if (!(authUserRole.equals(CommunityRole.ADMIN) || authUserRole.equals(CommunityRole.MODERATOR))) {
            throw new NotAllowedException(USER_NOT_ALLOWED);
        }
        userCommunityRepository.delete(userCommunityRepository.findByCommunityIdAndUserId(communityId, userId).orElseThrow(new NotFoundException(USER_NOT_FOUND_IN_COMMUNITY)));
    }

    @Transactional
    public void leaveCommunity(UUID communityId, UUID authUserId) {
        userCommunityRepository.delete(userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId).orElseThrow(new NotFoundException(USER_NOT_FOUND_IN_COMMUNITY)));
    }

    @Transactional
    public void updateCommunity(CommunityUpdateDTO community, UUID communityId, UUID userId) {
        CommunityRole authUserRole = getCurrentUserRole(communityId, userId);
        if (!(authUserRole.equals(CommunityRole.ADMIN) || authUserRole.equals(CommunityRole.MODERATOR))) {
            throw new NotAllowedException(USER_NOT_ALLOWED);
        }
        Community currentCommunity = communityRepository.findById(communityId).get();

        currentCommunity.setName((community.getName() != null && !community.getName().isEmpty())
                ? community.getName()
                : currentCommunity.getName());

        currentCommunity.setCategory((community.getCategory() != null && !community.getCategory().toString().isEmpty())
                ? community.getCategory()
                : currentCommunity.getCategory());

        currentCommunity.setDescription((community.getDescription() != null && !community.getDescription().isEmpty())
                ? community.getDescription()
                : currentCommunity.getDescription());

        currentCommunity.setPhotoLink((community.getPhotoLink() != null && !community.getPhotoLink().isEmpty())
                ? community.getPhotoLink()
                : currentCommunity.getPhotoLink());

        currentCommunity.setPublishPermission((community.getPublishPermission() != null && !community.getPublishPermission().toString().isEmpty())
                ? community.getPublishPermission()
                : currentCommunity.getPublishPermission());

        currentCommunity.setJoinType((community.getJoinType() != null && !community.getJoinType().toString().isEmpty())
                ? community.getJoinType()
                : currentCommunity.getJoinType());

        communityRepository.save(currentCommunity);
    }

    public List<Community> searchCommunityByName(String name) {
        return communityRepository.findByNameContainingIgnoreCase(name);
    }

    public Set<User> getCommunityJoinRequests(UUID communityId) {
        return userCommunityRepository.findByCommunityId(communityId).stream().filter(request -> request.getUserRole().equals(CommunityRole.WAITING_TO_ACCEPT)).map(UserCommunity::getUser).collect(Collectors.toSet());
    }

    public CommunityRole getCurrentUserRole(UUID communityId, UUID authUserId) {
        return userCommunityRepository.findByCommunityIdAndUserId(communityId, authUserId).map(UserCommunity::getUserRole).orElseThrow(new NotFoundException(USER_NOT_FOUND_IN_COMMUNITY));
    }

    public Set<User> getFollowers(UUID communityId) {
        return userCommunityRepository.findByCommunityId(communityId).stream().filter(userCommunity -> !userCommunity.getUserRole().equals(CommunityRole.WAITING_TO_ACCEPT)).map(UserCommunity::getUser).collect(Collectors.toSet());
    }

}
