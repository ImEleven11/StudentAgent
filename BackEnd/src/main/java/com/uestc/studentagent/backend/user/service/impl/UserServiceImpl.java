package com.uestc.studentagent.backend.user.service.impl;

import com.uestc.studentagent.backend.common.error.ErrorCode;
import com.uestc.studentagent.backend.common.exception.BusinessException;
import com.uestc.studentagent.backend.security.JwtTokenProvider;
import com.uestc.studentagent.backend.user.dto.LoginRequest;
import com.uestc.studentagent.backend.user.dto.LoginResponse;
import com.uestc.studentagent.backend.user.dto.RegisterRequest;
import com.uestc.studentagent.backend.user.dto.RegisterResponse;
import com.uestc.studentagent.backend.user.dto.UpdateUserProfileRequest;
import com.uestc.studentagent.backend.user.dto.UserProfileResponse;
import com.uestc.studentagent.backend.user.entity.UserEntity;
import com.uestc.studentagent.backend.user.entity.UserRole;
import com.uestc.studentagent.backend.user.entity.UserStatus;
import com.uestc.studentagent.backend.user.mapper.UserMapper;
import com.uestc.studentagent.backend.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceImpl(UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String email = normalize(request.getEmail()).toLowerCase();

        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "username already exists");
        }
        if (userMapper.findByEmail(email) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "email already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(username);
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);

        userMapper.insert(user);
        return new RegisterResponse(user.getId());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String account = normalize(request.getAccount());
        UserEntity user = userMapper.findByAccount(account);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "invalid username/email or password");
        }
        ensureActive(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        return new LoginResponse(accessToken, "Bearer", UserProfileResponse.from(user));
    }

    @Override
    public UserProfileResponse getProfile(Long userId) {
        return UserProfileResponse.from(loadActiveUser(userId));
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request) {
        UserEntity user = loadActiveUser(userId);

        user.setNickname(normalizeNullable(request.getNickname(), user.getNickname()));
        user.setAvatar(normalizeNullable(request.getAvatar(), user.getAvatar()));
        user.setPhone(normalizeNullable(request.getPhone(), user.getPhone()));

        userMapper.updateProfile(user);
        return UserProfileResponse.from(loadActiveUser(userId));
    }

    @Override
    public UserEntity loadActiveUser(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "user not found");
        }
        ensureActive(user);
        return user;
    }

    private void ensureActive(UserEntity user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "user account is disabled");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String incomingValue, String currentValue) {
        if (!StringUtils.hasText(incomingValue)) {
            return currentValue;
        }
        return incomingValue.trim();
    }
}
