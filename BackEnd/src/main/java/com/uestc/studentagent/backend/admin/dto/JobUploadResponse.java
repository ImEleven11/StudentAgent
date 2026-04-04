package com.uestc.studentagent.backend.admin.dto;

public record JobUploadResponse(
        int importedCount,
        int failedCount
) {
}
