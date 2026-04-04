package com.uestc.studentagent.backend.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class AssessmentCreateRequest {

    @NotNull(message = "planId is required")
    private Long planId;

    @NotBlank(message = "type is required")
    @Pattern(regexp = "monthly|quarterly", message = "type must be monthly or quarterly")
    private String type;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
