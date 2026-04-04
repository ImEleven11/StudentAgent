package com.uestc.studentagent.backend.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MarketForecastRequest {

    @NotNull(message = "jobId is required")
    private Long jobId;

    @NotBlank(message = "period is required")
    private String period;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
