package com.uestc.studentagent.backend.plan.dto;

import java.math.BigDecimal;
import java.util.List;

public record PlanProgressResponse(
        Long planId,
        BigDecimal progress,
        List<PlanTaskResponse> completedTasks
) {
}
