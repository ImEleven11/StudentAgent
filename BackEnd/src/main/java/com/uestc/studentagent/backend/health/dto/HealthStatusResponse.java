package com.uestc.studentagent.backend.health.dto;

import java.time.OffsetDateTime;

public record HealthStatusResponse(String status, String service, OffsetDateTime timestamp) {
}
