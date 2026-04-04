package com.uestc.studentagent.backend.plan.dto;

import com.uestc.studentagent.backend.plan.entity.PlanTaskEntity;

public record PlanTaskResponse(
        Long taskId,
        String phaseName,
        String taskTitle,
        String description,
        Integer recommendedDays,
        Boolean completed,
        Integer sortOrder
) {
    public static PlanTaskResponse from(PlanTaskEntity entity) {
        return new PlanTaskResponse(
                entity.getId(),
                entity.getPhaseName(),
                entity.getTaskTitle(),
                entity.getDescription(),
                entity.getRecommendedDays(),
                entity.getCompleted(),
                entity.getSortOrder()
        );
    }
}
