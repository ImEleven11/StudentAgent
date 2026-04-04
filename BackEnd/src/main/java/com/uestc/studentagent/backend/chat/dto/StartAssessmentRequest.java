package com.uestc.studentagent.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class StartAssessmentRequest {

    @NotBlank(message = "type is required")
    @Pattern(regexp = "interest|personality|risk", message = "type must be interest, personality, or risk")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
