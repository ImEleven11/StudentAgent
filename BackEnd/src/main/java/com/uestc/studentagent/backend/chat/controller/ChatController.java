package com.uestc.studentagent.backend.chat.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.chat.dto.ChatHistoryResponse;
import com.uestc.studentagent.backend.chat.dto.ChatReplyResponse;
import com.uestc.studentagent.backend.chat.dto.ChatSendRequest;
import com.uestc.studentagent.backend.chat.dto.ChatSessionStartResponse;
import com.uestc.studentagent.backend.chat.dto.CompleteMissingRequest;
import com.uestc.studentagent.backend.chat.dto.StartAssessmentRequest;
import com.uestc.studentagent.backend.chat.service.ChatService;
import com.uestc.studentagent.backend.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    @Operation(summary = "Send a chat message")
    public ApiResponse<ChatReplyResponse> send(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                               @Valid @RequestBody ChatSendRequest request) {
        return ApiResponse.success(chatService.send(principal.getUserId(), request));
    }

    @GetMapping("/history")
    @Operation(summary = "Get chat history by session")
    public ApiResponse<ChatHistoryResponse> history(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                    @RequestParam String sessionId,
                                                    @RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer size) {
        return ApiResponse.success(chatService.history(principal.getUserId(), sessionId, page, size));
    }

    @PostMapping("/start-assessment")
    @Operation(summary = "Start an assessment chat")
    public ApiResponse<ChatSessionStartResponse> startAssessment(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                                 @Valid @RequestBody StartAssessmentRequest request) {
        return ApiResponse.success(chatService.startAssessment(principal.getUserId(), request));
    }

    @PostMapping("/complete-missing")
    @Operation(summary = "Start a guided chat for missing profile fields")
    public ApiResponse<ChatSessionStartResponse> completeMissing(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                                                 @Valid @RequestBody CompleteMissingRequest request) {
        return ApiResponse.success(chatService.startMissingFieldCompletion(principal.getUserId(), request));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "Delete a chat session and its history")
    public ApiResponse<Void> clearSession(@AuthenticationPrincipal SecurityUserPrincipal principal,
                                          @PathVariable String sessionId) {
        chatService.clearSession(principal.getUserId(), sessionId);
        return ApiResponse.success();
    }
}
