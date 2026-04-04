package com.uestc.studentagent.backend.match.dto;

import java.math.BigDecimal;
import java.util.List;

public record MatchRecommendationItemResponse(
        Long jobId,
        String jobTitle,
        String industry,
        String location,
        BigDecimal matchScore,
        List<String> matchedSkills,
        List<String> missingSkills
) {
}
