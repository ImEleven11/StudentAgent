package com.uestc.studentagent.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatSendRequest {

    @NotBlank(message = "message is required")
    @Size(max = 4000, message = "message is too long")
    private String message;

    @Size(max = 64, message = "sessionId is too long")
    private String sessionId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
