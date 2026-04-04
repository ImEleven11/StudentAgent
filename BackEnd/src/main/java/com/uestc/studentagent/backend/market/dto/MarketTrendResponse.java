package com.uestc.studentagent.backend.market.dto;

public record MarketTrendResponse(
        String industry,
        String jobCountTrend,
        String salaryTrend,
        String talentGap
) {
}
