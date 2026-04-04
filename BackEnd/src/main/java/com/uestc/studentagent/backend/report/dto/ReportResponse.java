package com.uestc.studentagent.backend.report.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReportResponse(
        Long reportId,
        Long profileId,
        Long targetJobId,
        String title,
        List<ReportSectionResponse> sections,
        String polishedContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
