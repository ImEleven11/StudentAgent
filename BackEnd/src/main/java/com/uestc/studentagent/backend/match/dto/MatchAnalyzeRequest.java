package com.uestc.studentagent.backend.match.dto;

import jakarta.validation.constraints.NotNull;

public class MatchAnalyzeRequest {

    @NotNull(message = "profileId is required")
    private Long profileId;

    private Long jobId;

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
}
