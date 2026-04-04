package com.uestc.studentagent.backend.chat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CompleteMissingRequest {

    @NotNull(message = "profileId is required")
    private Long profileId;

    @NotEmpty(message = "missingFields is required")
    private List<String> missingFields;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields;
    }
}
