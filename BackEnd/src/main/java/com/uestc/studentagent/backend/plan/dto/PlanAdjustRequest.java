package com.uestc.studentagent.backend.plan.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class PlanAdjustRequest {

    @Size(max = 255, message = "title is too long")
    private String title;

    @Size(max = 4000, message = "summary is too long")
    private String summary;

    private List<PlanTaskAdjustRequest> adjustments;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<PlanTaskAdjustRequest> getAdjustments() {
        return adjustments;
    }

    public void setAdjustments(List<PlanTaskAdjustRequest> adjustments) {
        this.adjustments = adjustments;
    }
}
