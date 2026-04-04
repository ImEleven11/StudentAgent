package com.uestc.studentagent.backend.user.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import com.uestc.studentagent.backend.user.dto.UpdateUserProfileRequest;
import com.uestc.studentagent.backend.user.dto.UserProfileResponse;
import com.uestc.studentagent.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile")
@Tag(name = "User Profile")
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get the current user profile")
    public ApiResponse<UserProfileResponse> getProfile(@AuthenticationPrincipal SecurityUserPrincipal principal) {
        return ApiResponse.success(userService.getProfile(principal.getUserId()));
    }

    @PutMapping
    @Operation(summary = "Update the current user profile")
    public ApiResponse<UserProfileResponse> updateProfile(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                          @Valid @RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.success(userService.updateProfile(principal.getUserId(), request));
    }
}
