package com.uestc.studentagent.backend.chat.dto;

public record ChatSessionStartResponse(
        String sessionId,
        ChatMessageResponse firstMessage
) {
}
