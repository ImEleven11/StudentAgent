package com.uestc.studentagent.backend.user.service;

import com.uestc.studentagent.backend.user.dto.LoginRequest;
import com.uestc.studentagent.backend.user.dto.LoginResponse;
import com.uestc.studentagent.backend.user.dto.RegisterRequest;
import com.uestc.studentagent.backend.user.dto.RegisterResponse;
import com.uestc.studentagent.backend.user.dto.UpdateUserProfileRequest;
import com.uestc.studentagent.backend.user.dto.UserProfileResponse;
import com.uestc.studentagent.backend.user.entity.UserEntity;

public interface UserService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserProfileResponse getProfile(Long userId);

    UserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request);

    UserEntity loadActiveUser(Long userId);
}
