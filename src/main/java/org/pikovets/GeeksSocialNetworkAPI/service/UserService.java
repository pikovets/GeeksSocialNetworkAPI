package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.exceptions.UnAuthorizedException;
import org.pikovets.GeeksSocialNetworkAPI.model.Post;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.pikovets.GeeksSocialNetworkAPI.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JwtUtils jwtUtils;

    @Autowired
    public UserService(UserRepository userRepository, PostRepository postRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.jwtUtils = jwtUtils;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(new NotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(new NotFoundException("User not found"));
    }

    public List<Post> getUserPosts(UUID authorId) {
        return postRepository.findByAuthorOrderByDateDesc(
                userRepository.findById(authorId).orElseThrow(new NotFoundException("User not found")));
    }

    @Transactional
    public void updateUser(User updatedUser, String token) {
        User userToBeUpdated = userRepository.findByEmail(jwtUtils.extractUsername(token))
                .orElseThrow(UnAuthorizedException::new);

        updatedUser.setId(userToBeUpdated.getId());
        enrichUser(userToBeUpdated.getId(), updatedUser);

        userRepository.save(updatedUser);
    }

    @Transactional
    public void deleteUser(String token) {
        User deletedUser = userRepository.findByEmail(jwtUtils.extractUsername(token))
                .orElseThrow(UnAuthorizedException::new);

        userRepository.delete(deletedUser);
    }

    public void enrichUser(UUID id, User expandableUser) {
        User userHelper = userRepository.findById(id).orElseThrow(new NotFoundException("User not found"));

        expandableUser.setIsActive(userHelper.getIsActive());
        expandableUser.setJoinedAt(userHelper.getJoinedAt());
        expandableUser.setRole(userHelper.getRole());
    }
}