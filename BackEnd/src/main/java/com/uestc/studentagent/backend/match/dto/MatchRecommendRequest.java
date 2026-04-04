package com.uestc.studentagent.backend.match.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MatchRecommendRequest {

    @NotNull(message = "profileId is required")
    private Long profileId;

    @Min(value = 1, message = "topN must be greater than 0")
    private Integer topN;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Integer getTopN() {
        return topN;
    }

    public void setTopN(Integer topN) {
        this.topN = topN;
    }
}
