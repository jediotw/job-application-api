package com.example.jobapplicationapi.company.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateCompanyRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String cin;

    private String website;

    private String description;

    public UpdateCompanyRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}