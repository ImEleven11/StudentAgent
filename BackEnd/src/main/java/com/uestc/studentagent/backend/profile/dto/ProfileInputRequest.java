package com.uestc.studentagent.backend.profile.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class ProfileInputRequest {

    @Size(max = 4000, message = "educationSummary is too long")
    private String educationSummary;

    private List<@Size(max = 128, message = "skill item is too long") String> skills;

    private List<@Size(max = 512, message = "experience item is too long") String> experiences;

    private List<@Size(max = 128, message = "certificate item is too long") String> certificates;

    @Size(max = 4000, message = "innovationSummary is too long")
    private String innovationSummary;

    @Size(max = 4000, message = "interestSummary is too long")
    private String interestSummary;

    @Size(max = 4000, message = "personalitySummary is too long")
    private String personalitySummary;

    public String getEducationSummary() {
        return educationSummary;
    }

    public void setEducationSummary(String educationSummary) {
        this.educationSummary = educationSummary;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<String> experiences) {
        this.experiences = experiences;
    }

    public List<String> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<String> certificates) {
        this.certificates = certificates;
    }

    public String getInnovationSummary() {
        return innovationSummary;
    }

    public void setInnovationSummary(String innovationSummary) {
        this.innovationSummary = innovationSummary;
    }

    public String getInterestSummary() {
        return interestSummary;
    }

    public void setInterestSummary(String interestSummary) {
        this.interestSummary = interestSummary;
    }

    public String getPersonalitySummary() {
        return personalitySummary;
    }

    public void setPersonalitySummary(String personalitySummary) {
        this.personalitySummary = personalitySummary;
    }
}
