package com.uestc.studentagent.backend.job.dto;

import java.util.List;

public record JobPathResponse(
        Long jobId,
        List<JobPathNodeResponse> promotionPaths,
        List<JobPathNodeResponse> transitionPaths
) {
}
