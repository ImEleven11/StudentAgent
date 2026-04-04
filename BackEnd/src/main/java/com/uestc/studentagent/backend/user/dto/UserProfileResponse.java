package com.uestc.studentagent.backend.user.dto;

import com.uestc.studentagent.backend.user.entity.UserEntity;
import com.uestc.studentagent.backend.user.entity.UserRole;
import com.uestc.studentagent.backend.user.entity.UserStatus;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String nickname,
        String avatar,
        String phone,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserProfileResponse from(UserEntity user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatar(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
