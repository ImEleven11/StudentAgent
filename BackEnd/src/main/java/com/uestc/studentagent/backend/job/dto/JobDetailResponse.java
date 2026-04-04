package com.uestc.studentagent.backend.job.dto;

import com.uestc.studentagent.backend.job.entity.JobEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record JobDetailResponse(
        Long jobId,
        String title,
        String industry,
        String location,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String description,
        List<String> skills,
        String portraitSummary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static JobDetailResponse from(JobEntity entity, List<String> skills) {
        return new JobDetailResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getIndustry(),
                entity.getLocation(),
                entity.getSalaryMin(),
                entity.getSalaryMax(),
                entity.getDescription(),
                skills,
                entity.getPortraitSummary(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
