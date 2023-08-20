package org.pikovets.GeeksSocialNetworkAPI.service;

import lombok.extern.slf4j.Slf4j;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.repository.UserRepository;
import org.pikovets.GeeksSocialNetworkAPI.core.CustomResponse;
import org.pikovets.GeeksSocialNetworkAPI.core.CustomStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public CustomResponse<User> getAllUsers() {
        List<User> users = userRepository.findAll();

        return new CustomResponse<>(users, CustomStatus.SUCCESS);
    }

    public CustomResponse<User> getUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow();

        return new CustomResponse<>(List.of(user), CustomStatus.SUCCESS);
    }

    public CustomResponse<User> getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        return new CustomResponse<>(List.of(user), CustomStatus.SUCCESS);
    }

    @Transactional
    public CustomResponse<User> saveUser(User user) {
        user.setIsActive(true);
        user.setJoinedAt(LocalDateTime.now());

        userRepository.save(user);

        return new CustomResponse<>(List.of(user), CustomStatus.SUCCESS);
    }

    @Transactional
    public CustomResponse<User> updateUser(User updatedUser, UUID id) {
        updatedUser.setId(id);
        enrichUser(updatedUser, id);

        userRepository.save(updatedUser);

        return new CustomResponse<>(List.of(updatedUser), CustomStatus.SUCCESS);
    }

    @Transactional
    public CustomResponse<User> deleteUser(UUID id) {
        User deletedUser = userRepository.findById(id).orElseThrow();
        userRepository.delete(deletedUser);

        return new CustomResponse<>(List.of(deletedUser), CustomStatus.SUCCESS);
    }

    public void enrichUser(User expandableUser, UUID id) {
        User userHelper = userRepository.findById(id).orElseThrow();

        expandableUser.setIsActive(userHelper.getIsActive());
        expandableUser.setJoinedAt(userHelper.getJoinedAt());
    }
}