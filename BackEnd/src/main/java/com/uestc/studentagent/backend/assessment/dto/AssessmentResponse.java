package com.uestc.studentagent.backend.assessment.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AssessmentResponse(
        Long assessmentId,
        Long planId,
        String type,
        String status,
        List<AssessmentQuestionResponse> questions,
        List<String> answers,
        AssessmentResultResponse result,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
