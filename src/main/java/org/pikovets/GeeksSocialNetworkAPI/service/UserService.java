package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        expandableUser.setRole(userHelper.getRole());
    }

    public User mergeUsers(User existingUser, User newUser) {
        User mergedUser;
        try {
            mergedUser = (User) existingUser.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }

        if (newUser.getFirstName() != null && !newUser.getFirstName().isEmpty()) {
            mergedUser.setFirstName(newUser.getFirstName());
        }

        if (newUser.getLastName() != null && !newUser.getLastName().isEmpty()) {
            mergedUser.setFirstName(newUser.getLastName());
        }

        if (newUser.getEmail() != null && !newUser.getEmail().isEmpty()) {
            mergedUser.setEmail(newUser.getEmail());
        }

        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
            mergedUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }

        return mergedUser;
    }
}