package com.uestc.studentagent.backend.match.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MatchAnalysisResponse(
        Long profileId,
        Long jobId,
        String jobTitle,
        BigDecimal overallScore,
        BigDecimal basicScore,
        BigDecimal skillScore,
        BigDecimal potentialScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        String summary,
        LocalDateTime createdAt
) {
}
