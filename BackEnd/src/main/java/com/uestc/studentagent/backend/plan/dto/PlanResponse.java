package com.uestc.studentagent.backend.plan.dto;

import com.uestc.studentagent.backend.plan.entity.PlanEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PlanResponse(
        Long planId,
        Long profileId,
        Long targetJobId,
        String title,
        String summary,
        String status,
        BigDecimal progress,
        List<PlanTaskResponse> tasks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlanResponse from(PlanEntity entity, List<PlanTaskResponse> tasks) {
        return new PlanResponse(
                entity.getId(),
                entity.getProfileId(),
                entity.getTargetJobId(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getStatus().name(),
                entity.getProgress(),
                tasks,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
