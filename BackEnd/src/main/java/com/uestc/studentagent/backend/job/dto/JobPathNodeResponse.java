package com.uestc.studentagent.backend.job.dto;

import com.uestc.studentagent.backend.job.entity.JobPathEntity;

public record JobPathNodeResponse(
        String pathType,
        Integer stepOrder,
        Long targetJobId,
        String targetJobTitle,
        String description
) {
    public static JobPathNodeResponse from(JobPathEntity entity) {
        return new JobPathNodeResponse(
                entity.getPathType().name(),
                entity.getStepOrder(),
                entity.getTargetJobId(),
                entity.getTargetJobTitle(),
                entity.getDescription()
        );
    }
}
