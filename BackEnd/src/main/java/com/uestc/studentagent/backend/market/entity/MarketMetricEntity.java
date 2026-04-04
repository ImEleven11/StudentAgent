package com.uestc.studentagent.backend.market.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketMetricEntity {

    private Long id;
    private Long jobId;
    private BigDecimal supplyDemandIndex;
    private String levelTag;
    private BigDecimal hotScore;
    private String demandTrend;
    private String salaryTrend;
    private String talentGap;
    private String forecastPeriod;
    private String forecastResult;
    private BigDecimal forecastConfidence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public BigDecimal getSupplyDemandIndex() {
        return supplyDemandIndex;
    }

    public void setSupplyDemandIndex(BigDecimal supplyDemandIndex) {
        this.supplyDemandIndex = supplyDemandIndex;
    }

    public String getLevelTag() {
        return levelTag;
    }

    public void setLevelTag(String levelTag) {
        this.levelTag = levelTag;
    }

    public BigDecimal getHotScore() {
        return hotScore;
    }

    public void setHotScore(BigDecimal hotScore) {
        this.hotScore = hotScore;
    }

    public String getDemandTrend() {
        return demandTrend;
    }

    public void setDemandTrend(String demandTrend) {
        this.demandTrend = demandTrend;
    }

    public String getSalaryTrend() {
        return salaryTrend;
    }

    public void setSalaryTrend(String salaryTrend) {
        this.salaryTrend = salaryTrend;
    }

    public String getTalentGap() {
        return talentGap;
    }

    public void setTalentGap(String talentGap) {
        this.talentGap = talentGap;
    }

    public String getForecastPeriod() {
        return forecastPeriod;
    }

    public void setForecastPeriod(String forecastPeriod) {
        this.forecastPeriod = forecastPeriod;
    }

    public String getForecastResult() {
        return forecastResult;
    }

    public void setForecastResult(String forecastResult) {
        this.forecastResult = forecastResult;
    }

    public BigDecimal getForecastConfidence() {
        return forecastConfidence;
    }

    public void setForecastConfidence(BigDecimal forecastConfidence) {
        this.forecastConfidence = forecastConfidence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
