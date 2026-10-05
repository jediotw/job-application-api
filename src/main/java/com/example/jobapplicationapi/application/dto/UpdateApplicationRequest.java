package com.example.jobapplicationapi.application.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateApplicationRequest {

  @NotBlank private String status;

  public UpdateApplicationRequest() {}

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
