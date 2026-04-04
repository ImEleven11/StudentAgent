package com.uestc.studentagent.backend.profile.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StudentProfileEntity {

    private Long id;
    private Long userId;
    private String educationSummary;
    private String skillsJson;
    private String experiencesJson;
    private String certificatesJson;
    private String innovationSummary;
    private String interestSummary;
    private String personalitySummary;
    private BigDecimal abilityScore;
    private BigDecimal competitivenessScore;
    private BigDecimal completenessScore;
    private String resumeFileName;
    private String resumeStoragePath;
    private String resumeContentType;
    private ProfileParseStatus parseStatus;
    private Integer parseProgress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public String getEducationSummary() {
        return educationSummary;
    }

    public void setEducationSummary(String educationSummary) {
        this.educationSummary = educationSummary;
    }

    public String getSkillsJson() {
        return skillsJson;
    }

    public void setSkillsJson(String skillsJson) {
        this.skillsJson = skillsJson;
    }

    public String getExperiencesJson() {
        return experiencesJson;
    }

    public void setExperiencesJson(String experiencesJson) {
        this.experiencesJson = experiencesJson;
    }

    public String getCertificatesJson() {
        return certificatesJson;
    }

    public void setCertificatesJson(String certificatesJson) {
        this.certificatesJson = certificatesJson;
    }

    public String getInnovationSummary() {
        return innovationSummary;
    }

    public void setInnovationSummary(String innovationSummary) {
        this.innovationSummary = innovationSummary;
    }

    public String getInterestSummary() {
        return interestSummary;
    }

    public void setInterestSummary(String interestSummary) {
        this.interestSummary = interestSummary;
    }

    public String getPersonalitySummary() {
        return personalitySummary;
    }

    public void setPersonalitySummary(String personalitySummary) {
        this.personalitySummary = personalitySummary;
    }

    public BigDecimal getAbilityScore() {
        return abilityScore;
    }

    public void setAbilityScore(BigDecimal abilityScore) {
        this.abilityScore = abilityScore;
    }

    public BigDecimal getCompetitivenessScore() {
        return competitivenessScore;
    }

    public void setCompetitivenessScore(BigDecimal competitivenessScore) {
        this.competitivenessScore = competitivenessScore;
    }

    public BigDecimal getCompletenessScore() {
        return completenessScore;
    }

    public void setCompletenessScore(BigDecimal completenessScore) {
        this.completenessScore = completenessScore;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public String getResumeStoragePath() {
        return resumeStoragePath;
    }

    public void setResumeStoragePath(String resumeStoragePath) {
        this.resumeStoragePath = resumeStoragePath;
    }

    public String getResumeContentType() {
        return resumeContentType;
    }

    public void setResumeContentType(String resumeContentType) {
        this.resumeContentType = resumeContentType;
    }

    public ProfileParseStatus getParseStatus() {
        return parseStatus;
    }

    public void setParseStatus(ProfileParseStatus parseStatus) {
        this.parseStatus = parseStatus;
    }

    public Integer getParseProgress() {
        return parseProgress;
    }

    public void setParseProgress(Integer parseProgress) {
        this.parseProgress = parseProgress;
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
