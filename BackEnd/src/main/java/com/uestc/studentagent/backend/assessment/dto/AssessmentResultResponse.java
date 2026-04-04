package com.uestc.studentagent.backend.assessment.dto;

import java.math.BigDecimal;
import java.util.List;

public record AssessmentResultResponse(
        BigDecimal score,
        List<String> suggestions
) {
}
