package com.uestc.studentagent.backend.resource.dto;

import com.uestc.studentagent.backend.resource.entity.ResourceEntity;

import java.util.List;

public record ResourceItemResponse(
        Long resourceId,
        String title,
        String description,
        String link,
        List<String> skills,
        String level,
        String difficulty,
        String location,
        String category,
        String timeRange,
        String duration
) {
    public static ResourceItemResponse from(ResourceEntity entity, List<String> skills) {
        return new ResourceItemResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getLink(),
                skills,
                entity.getLevelTag(),
                entity.getDifficultyTag(),
                entity.getLocationTag(),
                entity.getCategoryTag(),
                entity.getTimeRangeTag(),
                entity.getDurationText()
        );
    }
}
