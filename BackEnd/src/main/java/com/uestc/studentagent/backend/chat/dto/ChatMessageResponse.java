package com.uestc.studentagent.backend.chat.dto;

import com.uestc.studentagent.backend.chat.entity.ChatMessageEntity;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        String role,
        String content,
        String structuredPayload,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessageEntity message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getStructuredPayload(),
                message.getCreatedAt()
        );
    }
}
