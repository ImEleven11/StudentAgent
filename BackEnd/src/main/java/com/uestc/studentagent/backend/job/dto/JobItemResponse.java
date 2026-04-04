package com.uestc.studentagent.backend.job.dto;

import com.uestc.studentagent.backend.job.entity.JobEntity;

import java.math.BigDecimal;

public record JobItemResponse(
        Long jobId,
        String title,
        String industry,
        String location,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String portraitSummary
) {
    public static JobItemResponse from(JobEntity entity) {
        return new JobItemResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getIndustry(),
                entity.getLocation(),
                entity.getSalaryMin(),
                entity.getSalaryMax(),
                entity.getPortraitSummary()
        );
    }
}
