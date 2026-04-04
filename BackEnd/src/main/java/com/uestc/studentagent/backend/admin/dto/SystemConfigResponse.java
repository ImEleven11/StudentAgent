package com.uestc.studentagent.backend.admin.dto;

import java.time.LocalDateTime;

public record SystemConfigResponse(
        String configKey,
        String configValue,
        LocalDateTime updatedAt
) {
}
