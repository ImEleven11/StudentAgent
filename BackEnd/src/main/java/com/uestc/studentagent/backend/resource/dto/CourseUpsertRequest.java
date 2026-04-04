package com.uestc.studentagent.backend.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CourseUpsertRequest {

    private Long resourceId;

    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title is too long")
    private String title;

    @Size(max = 4000, message = "description is too long")
    private String description;

    @Size(max = 1024, message = "link is too long")
    private String link;

    private List<@Size(max = 128, message = "skill item is too long") String> skills;

    @Size(max = 64, message = "level is too long")
    private String level;

    @Size(max = 128, message = "duration is too long")
    private String duration;

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
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

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
