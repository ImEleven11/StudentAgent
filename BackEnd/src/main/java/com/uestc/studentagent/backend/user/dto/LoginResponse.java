package com.uestc.studentagent.backend.user.dto;

public record LoginResponse(String accessToken, String tokenType, UserProfileResponse user) {
}
