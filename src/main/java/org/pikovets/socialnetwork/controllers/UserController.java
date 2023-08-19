package org.pikovets.socialnetwork.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.pikovets.socialnetwork.dto.UserDTO;
import org.pikovets.socialnetwork.model.User;
import org.pikovets.socialnetwork.service.UserService;
import org.pikovets.socialnetwork.core.CustomResponse;
import org.pikovets.socialnetwork.core.CustomStatus;
import org.pikovets.socialnetwork.core.ErrorsUtil;
import org.pikovets.socialnetwork.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final UserValidator userValidator;

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper, UserValidator userValidator) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
    }

    @GetMapping
    public CustomResponse<UserDTO> getAllUsers() {
        CustomResponse<User> userResponse = userService.getAllUsers();

        return new CustomResponse<>(
                userResponse.getResponseList().stream().map(this::convertToUserDTO).toList(),
                CustomStatus.values()[userResponse.getCode()]);
    }

    @GetMapping("/signin")
    public CustomResponse<UserDTO> signIn(@RequestParam("id") UUID id) {
        CustomResponse<User> userResponse = userService.getUserById(id);

        return new CustomResponse<>(
                userResponse.getResponseList().stream().map(this::convertToUserDTO).toList(),
                CustomStatus.values()[userResponse.getCode()]);
    }

    @PostMapping("/signup")
    public CustomResponse<UserDTO> signUp(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
        User user = convertToUser(userDTO);
        CustomResponse<User> userResponse;

        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            userResponse = ErrorsUtil.returnBadRequestException(user, bindingResult);
        } else {
            userResponse = userService.saveUser(user);
        }

        return new CustomResponse<>(
                userResponse.getCode(),
                userResponse.getMessage(),
                userResponse.getResponseList().stream().map(this::convertToUserDTO).toList());
    }

    @PatchMapping("/{id}")
    public CustomResponse<UserDTO> updateUser(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult,
                                          @PathVariable("id") UUID id) {
        User user = convertToUser(userDTO);
        CustomResponse<User> userResponse;

        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            userResponse = ErrorsUtil.returnBadRequestException(user, bindingResult);
        } else {
            userResponse = userService.updateUser(user, id);
        }

        return new CustomResponse<>(
                userResponse.getCode(),
                userResponse.getMessage(),
                userResponse.getResponseList().stream().map(this::convertToUserDTO).toList());
    }

    @DeleteMapping("/{id}")
    public CustomResponse<UserDTO> deleteUser(@PathVariable("id") UUID id) {
        CustomResponse<User> userResponse = userService.deleteUser(id);

        return new CustomResponse<>(
                userResponse.getResponseList().stream().map(this::convertToUserDTO).toList(),
                CustomStatus.values()[userResponse.getCode()]);
    }

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}