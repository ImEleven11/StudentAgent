package com.uestc.studentagent.backend.admin.dto;

public record SystemStatsResponse(
        long userCount,
        long profileCount,
        long matchCount,
        long planCount,
        long reportCount
) {
}
