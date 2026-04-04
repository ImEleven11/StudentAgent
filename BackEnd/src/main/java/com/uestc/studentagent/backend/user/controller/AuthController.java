package com.uestc.studentagent.backend.user.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import com.uestc.studentagent.backend.user.dto.LoginRequest;
import com.uestc.studentagent.backend.user.dto.LoginResponse;
import com.uestc.studentagent.backend.user.dto.RegisterRequest;
import com.uestc.studentagent.backend.user.dto.RegisterResponse;
import com.uestc.studentagent.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Authentication")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login by username or email")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout the current user")
    public ApiResponse<Void> logout(@AuthenticationPrincipal SecurityUserPrincipal principal) {
        return ApiResponse.success();
    }
}
