package com.uestc.studentagent.backend.market.dto;

import java.math.BigDecimal;

public record HotJobResponse(
        Long jobId,
        String title,
        String industry,
        String city,
        BigDecimal hotScore
) {
}
