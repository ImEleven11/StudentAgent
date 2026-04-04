package com.uestc.studentagent.backend.health.controller;

import com.uestc.studentagent.backend.api.ApiResponse;
import com.uestc.studentagent.backend.health.dto.HealthStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check backend health")
    public ApiResponse<HealthStatusResponse> health() {
        return ApiResponse.success(new HealthStatusResponse(
                "UP",
                "student-agent-backend",
                OffsetDateTime.now()
        ));
    }
}
