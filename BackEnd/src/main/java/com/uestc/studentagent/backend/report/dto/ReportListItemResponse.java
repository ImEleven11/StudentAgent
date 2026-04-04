package com.uestc.studentagent.backend.report.dto;

import java.time.LocalDateTime;

public record ReportListItemResponse(
        Long reportId,
        Long profileId,
        Long targetJobId,
        String title,
        LocalDateTime createdAt
) {
}
