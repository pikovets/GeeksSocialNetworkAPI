package org.pikovets.GeeksSocialNetworkAPI.service;

import lombok.extern.slf4j.Slf4j;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.UserNotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public User updateUser(User updatedUser, UUID id) {
        updatedUser.setId(id);
        enrichUser(updatedUser, id);

        userRepository.save(updatedUser);

        return updatedUser;
    }

    @Transactional
    public void deleteUser(UUID id) {
        User deletedUser = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        userRepository.delete(deletedUser);
    }

    public void enrichUser(User expandableUser, UUID id) {
        User userHelper = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        expandableUser.setIsActive(userHelper.getIsActive());
        expandableUser.setJoinedAt(userHelper.getJoinedAt());
        expandableUser.setRole(userHelper.getRole());
    }
}