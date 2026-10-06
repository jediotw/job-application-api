package com.example.jobapplicationapi.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationException(
      MethodArgumentNotValidException exception) {

    Map<String, String> errors = new HashMap<>();

    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {

      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ApiErrorResponse response =
        new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
      ResourceNotFoundException exception) {

    ApiErrorResponse response =
        new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception) {

    ApiErrorResponse response =
        new ApiErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(DbActionExecutionException.class)
  public ResponseEntity<ApiErrorResponse> handleDatabaseException(
      DbActionExecutionException exception) {

    Throwable cause = exception.getCause();

    if (cause instanceof DuplicateKeyException) {

      ApiErrorResponse response =
          new ApiErrorResponse(HttpStatus.CONFLICT.value(), "Data conflict");

      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    if (cause instanceof DataIntegrityViolationException) {

      ApiErrorResponse response =
          new ApiErrorResponse(HttpStatus.CONFLICT.value(), "Data conflict");

      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    ApiErrorResponse response =
        new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException exception) {

    ApiErrorResponse response = new ApiErrorResponse(HttpStatus.CONFLICT.value(), "Data conflict");

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {

    exception.printStackTrace();

    ApiErrorResponse response =
        new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(DataConflictException.class)
  public ResponseEntity<String> handleDataConflict(DataConflictException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<String> handleAuthenticationException(AuthenticationException exception) {

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
  }
}
