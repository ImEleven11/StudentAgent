package com.uestc.studentagent.backend.match.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MatchRecordEntity {

    private Long id;
    private Long userId;
    private Long profileId;
    private Long jobId;
    private BigDecimal overallScore;
    private BigDecimal basicScore;
    private BigDecimal skillScore;
    private BigDecimal potentialScore;
    private String detailsJson;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public BigDecimal getBasicScore() {
        return basicScore;
    }

    public void setBasicScore(BigDecimal basicScore) {
        this.basicScore = basicScore;
    }

    public BigDecimal getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(BigDecimal skillScore) {
        this.skillScore = skillScore;
    }

    public BigDecimal getPotentialScore() {
        return potentialScore;
    }

    public void setPotentialScore(BigDecimal potentialScore) {
        this.potentialScore = potentialScore;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
