package com.example.estoque.api.controller.user;

import com.example.estoque.api.dto.user.UserCreateDTO;
import com.example.estoque.api.dto.user.UserResponseDTO;
import com.example.estoque.api.mapper.user.UserMapper;
import com.example.estoque.application.service.user.UserService;
import com.example.estoque.domain.model.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserCreateDTO dto) {
        User user = userMapper.toEntity(dto);
        User savedUser = userService.registerUser(user);
        return new ResponseEntity<>(userMapper.toDTO(savedUser), HttpStatus.CREATED);
    }
}
