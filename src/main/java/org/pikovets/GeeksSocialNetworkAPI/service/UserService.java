package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.PostRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
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

    @Transactional
    public void updateUser(User updatedUser, UUID id) {
        updatedUser.setId(id);
        enrichUser(updatedUser, id);

        userRepository.save(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User deletedUser = userRepository.findById(id).orElseThrow(new NotFoundException("User not found"));
        userRepository.delete(deletedUser);
    }

    public void enrichUser(User expandableUser, UUID id) {
        User userHelper = userRepository.findById(id).orElseThrow(new NotFoundException("User not found"));

        expandableUser.setIsActive(userHelper.getIsActive());
        expandableUser.setJoinedAt(userHelper.getJoinedAt());
        expandableUser.setRole(userHelper.getRole());
    }
}