package com.uestc.studentagent.backend.chat.dto;

public record ChatReplyResponse(
        String sessionId,
        ChatMessageResponse reply
) {
}
