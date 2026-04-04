package com.uestc.studentagent.backend.chat.dto;

import java.util.List;

public record ChatHistoryResponse(
        String sessionId,
        Integer page,
        Integer size,
        Long total,
        List<ChatMessageResponse> messages
) {
}
