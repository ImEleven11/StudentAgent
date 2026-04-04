package com.uestc.studentagent.backend.market.dto;

import java.math.BigDecimal;

public record MarketForecastResponse(
        Long jobId,
        String period,
        String result,
        BigDecimal confidence
) {
}
