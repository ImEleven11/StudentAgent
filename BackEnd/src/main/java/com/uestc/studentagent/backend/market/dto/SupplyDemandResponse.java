package com.uestc.studentagent.backend.market.dto;

import java.math.BigDecimal;
import java.util.List;

public record SupplyDemandResponse(
        Long jobId,
        BigDecimal supplyDemandIndex,
        String level,
        List<BigDecimal> historicalTrend
) {
}
