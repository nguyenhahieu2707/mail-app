package com.nghhieu27.mail.demo.controller;

import com.nghhieu27.mail.demo.dto.request.ApiResponse;
import com.nghhieu27.mail.demo.dto.request.UserCreationRequest;
import com.nghhieu27.mail.demo.dto.response.UserResponse;
import com.nghhieu27.mail.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        log.info("Received request to create user: {}", request.getEmail());
        var result = userService.createUser(request);
        log.info("User created successfully: {}", result.getEmail());
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Create Successfully!")
                .result(result)
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        log.info("Fetching all users");
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable String userId) {
        log.info("Fetching user with ID: {}", userId);
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable String userId) {
        log.info("Deleting user with ID: {}", userId);
        userService.deleteUser(userId);
        return "User has been deleted!";
    }
}

