package com.example.jobapplicationapi.exception;

import java.util.Map;

public class ApiErrorResponse {

  private int status;
  private String message;
  private Map<String, String> errors;

  // For errors that have field-level details
  public ApiErrorResponse(int status, String message, Map<String, String> errors) {

    this.status = status;
    this.message = message;
    this.errors = errors;
  }

  // For simple errors
  public ApiErrorResponse(int status, String message) {

    this.status = status;
    this.message = message;
    this.errors = null;
  }

  public int getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public Map<String, String> getErrors() {
    return errors;
  }
}
