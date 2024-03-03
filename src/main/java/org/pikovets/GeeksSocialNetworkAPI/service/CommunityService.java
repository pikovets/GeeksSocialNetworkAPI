package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.dto.community.CommunityResponse;
import org.pikovets.GeeksSocialNetworkAPI.dto.community.CreateCommunityRequest;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.Community;
import org.pikovets.GeeksSocialNetworkAPI.model.UserCommunity;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.Role;
import org.pikovets.GeeksSocialNetworkAPI.repository.CommunityRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserCommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final UserCommunityRepository userCommunityRepository;

    @Autowired
    public CommunityService(CommunityRepository communityRepository, UserCommunityRepository userCommunityRepository) {
        this.communityRepository = communityRepository;
        this.userCommunityRepository = userCommunityRepository;
    }

    public List<Community> getAll() {
        return communityRepository.findAll();
    }

    public Community getById(UUID communityId, UUID authUserId) {
        Optional<UserCommunity> userCommunity = userCommunityRepository.findByUserIdAndCommunityId(authUserId, communityId);

        if (userCommunity.isEmpty()) {
            Community community = communityRepository.findById(communityId).orElseThrow(new NotFoundException("Community not found"));
            community.setPosts(null);
            return community;
        }
        return userCommunity.get().getCommunity();
    }

    @Transactional
    public void createCommunity(CreateCommunityRequest communityRequest, UUID adminId) {
        Community community = new Community();

        community.setName(communityRequest.getName());
        community.setDescription(communityRequest.getDescription());
        community.setCategory(communityRequest.getCategory());
        community.setPhotoLink(communityRequest.getPhotoLink());
        community.setPublishPermission(communityRequest.getPublishPermission());
        community.setJoinType(communityRequest.getJoinType());

        communityRepository.save(community);
    }
}
