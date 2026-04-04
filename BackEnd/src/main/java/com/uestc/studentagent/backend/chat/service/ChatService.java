package com.uestc.studentagent.backend.chat.service;

import com.uestc.studentagent.backend.chat.dto.ChatHistoryResponse;
import com.uestc.studentagent.backend.chat.dto.ChatReplyResponse;
import com.uestc.studentagent.backend.chat.dto.ChatSendRequest;
import com.uestc.studentagent.backend.chat.dto.ChatSessionStartResponse;
import com.uestc.studentagent.backend.chat.dto.CompleteMissingRequest;
import com.uestc.studentagent.backend.chat.dto.StartAssessmentRequest;

public interface ChatService {

    ChatReplyResponse send(Long userId, ChatSendRequest request);

    ChatHistoryResponse history(Long userId, String sessionId, Integer page, Integer size);

    ChatSessionStartResponse startAssessment(Long userId, StartAssessmentRequest request);

    ChatSessionStartResponse startMissingFieldCompletion(Long userId, CompleteMissingRequest request);

    void clearSession(Long userId, String sessionId);
}
