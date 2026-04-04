package com.uestc.studentagent.backend.match.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MatchHistoryItemResponse(
        Long recordId,
        Long profileId,
        Long jobId,
        BigDecimal overallScore,
        BigDecimal basicScore,
        BigDecimal skillScore,
        BigDecimal potentialScore,
        LocalDateTime createdAt
) {
}
