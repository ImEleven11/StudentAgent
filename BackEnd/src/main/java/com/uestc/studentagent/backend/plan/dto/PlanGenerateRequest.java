package com.uestc.studentagent.backend.plan.dto;

import jakarta.validation.constraints.NotNull;

public class PlanGenerateRequest {

    @NotNull(message = "profileId is required")
    private Long profileId;

    private Long targetJobId;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getTargetJobId() {
        return targetJobId;
    }

    public void setTargetJobId(Long targetJobId) {
        this.targetJobId = targetJobId;
    }
}
