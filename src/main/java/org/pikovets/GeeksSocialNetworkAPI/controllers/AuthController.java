package org.pikovets.GeeksSocialNetworkAPI.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.pikovets.GeeksSocialNetworkAPI.core.CustomResponse;
import org.pikovets.GeeksSocialNetworkAPI.core.ErrorUtils;
import org.pikovets.GeeksSocialNetworkAPI.dto.UserDTO;
import org.pikovets.GeeksSocialNetworkAPI.model.User;
import org.pikovets.GeeksSocialNetworkAPI.service.AuthService;
import org.pikovets.GeeksSocialNetworkAPI.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserValidator userValidator;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthController(AuthService authService, UserValidator userValidator, ModelMapper modelMapper) {
        this.authService = authService;
        this.userValidator = userValidator;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/register")
    public CustomResponse<String> registerUser(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
        User user = convertToUser(userDTO);
        CustomResponse<String> jwtResponse;

        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            jwtResponse = ErrorUtils.returnBadRequestException(bindingResult);
        } else {
            jwtResponse = authService.registerUser(user);
        }

        return new CustomResponse<>(
                jwtResponse.getCode(),
                jwtResponse.getMessage(),
                jwtResponse.getResponseList());
    }

    @GetMapping("/login")
    public CustomResponse<String> loginUser(@RequestParam("username") String username, @RequestParam("password") String password)  {
        CustomResponse<String> jwtResponse;

        jwtResponse = authService.loginUser(username, password);

        return new CustomResponse<>(
                jwtResponse.getCode(),
                jwtResponse.getMessage(),
                jwtResponse.getResponseList());
    }

    public User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}
