package com.uestc.studentagent.backend.profile.dto;

import com.uestc.studentagent.backend.profile.entity.StudentProfileEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StudentProfileResponse(
        Long profileId,
        Long userId,
        String educationSummary,
        List<String> skills,
        List<String> experiences,
        List<String> certificates,
        String innovationSummary,
        String interestSummary,
        String personalitySummary,
        BigDecimal abilityScore,
        BigDecimal competitivenessScore,
        BigDecimal completenessScore,
        String resumeFileName,
        String parseStatus,
        Integer parseProgress,
        List<String> missingFields,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StudentProfileResponse from(StudentProfileEntity entity,
                                              List<String> skills,
                                              List<String> experiences,
                                              List<String> certificates,
                                              List<String> missingFields) {
        return new StudentProfileResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getEducationSummary(),
                skills,
                experiences,
                certificates,
                entity.getInnovationSummary(),
                entity.getInterestSummary(),
                entity.getPersonalitySummary(),
                entity.getAbilityScore(),
                entity.getCompetitivenessScore(),
                entity.getCompletenessScore(),
                entity.getResumeFileName(),
                entity.getParseStatus().name(),
                entity.getParseProgress(),
                missingFields,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
