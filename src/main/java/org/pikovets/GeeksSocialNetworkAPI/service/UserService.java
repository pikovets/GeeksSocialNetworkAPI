package org.pikovets.GeeksSocialNetworkAPI.service;

import org.pikovets.GeeksSocialNetworkAPI.exceptions.NotFoundException;
import org.pikovets.GeeksSocialNetworkAPI.model.enums.RelationshipType;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.model.UserRelationship;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRelationshipRepository;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, UserRelationshipRepository userRelationshipRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRelationshipRepository = userRelationshipRepository;
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

    public List<User> getUsersByName(String name) {
        return userRepository.findAll().stream().filter(user -> (user.getFirstName() + user.getLastName()).contains(name)).toList();
    }

    public List<User> getFriends(UUID userId) {
        return Stream.concat(getUserById(userId).getFriendshipsRequested().stream().filter(request -> request.getType().equals(RelationshipType.FRIENDS)).map(UserRelationship::getAcceptor),
                getUserById(userId).getFriendshipsAccepted().stream().filter(request -> request.getType().equals(RelationshipType.FRIENDS)).map(UserRelationship::getRequester)).toList();
    }

    public List<User> getAcceptFriendRequests(UUID userId) {
        return getUserById(userId).getFriendshipsAccepted().stream().filter(request -> request.getType().equals(RelationshipType.ACCEPTOR_PENDING)).map(UserRelationship::getRequester).toList();
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
            mergedUser.setLastName(newUser.getLastName());
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