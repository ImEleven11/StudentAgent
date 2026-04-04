package com.uestc.studentagent.backend.plan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PlanTaskAdjustRequest {

    @NotNull(message = "taskId is required")
    private Long taskId;

    @Size(max = 128, message = "phaseName is too long")
    private String phaseName;

    @Size(max = 255, message = "taskTitle is too long")
    private String taskTitle;

    @Size(max = 4000, message = "description is too long")
    private String description;

    private Integer recommendedDays;

    private Boolean completed;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRecommendedDays() {
        return recommendedDays;
    }

    public void setRecommendedDays(Integer recommendedDays) {
        this.recommendedDays = recommendedDays;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
