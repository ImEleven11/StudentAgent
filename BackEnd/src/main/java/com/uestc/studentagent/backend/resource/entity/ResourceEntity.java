package com.uestc.studentagent.backend.resource.entity;

import java.time.LocalDateTime;

public class ResourceEntity {

    private Long id;
    private ResourceType resourceType;
    private String title;
    private String description;
    private String link;
    private String skillsJson;
    private String levelTag;
    private String difficultyTag;
    private String locationTag;
    private String categoryTag;
    private String timeRangeTag;
    private String durationText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSkillsJson() {
        return skillsJson;
    }

    public void setSkillsJson(String skillsJson) {
        this.skillsJson = skillsJson;
    }

    public String getLevelTag() {
        return levelTag;
    }

    public void setLevelTag(String levelTag) {
        this.levelTag = levelTag;
    }

    public String getDifficultyTag() {
        return difficultyTag;
    }

    public void setDifficultyTag(String difficultyTag) {
        this.difficultyTag = difficultyTag;
    }

    public String getLocationTag() {
        return locationTag;
    }

    public void setLocationTag(String locationTag) {
        this.locationTag = locationTag;
    }

    public String getCategoryTag() {
        return categoryTag;
    }

    public void setCategoryTag(String categoryTag) {
        this.categoryTag = categoryTag;
    }

    public String getTimeRangeTag() {
        return timeRangeTag;
    }

    public void setTimeRangeTag(String timeRangeTag) {
        this.timeRangeTag = timeRangeTag;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
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
