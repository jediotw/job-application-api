package com.example.jobapplicationapi.candidate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateCandidateRequest {
    private String name;
    private String email;
    private String phone;
    @JsonProperty("resume_url")
    private String resumeUrl;

    public UpdateCandidateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }
}
