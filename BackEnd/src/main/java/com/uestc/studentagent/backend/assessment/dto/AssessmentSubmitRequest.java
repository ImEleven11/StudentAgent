package com.uestc.studentagent.backend.assessment.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AssessmentSubmitRequest {

    @NotEmpty(message = "answers are required")
    private List<String> answers;

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }
}
