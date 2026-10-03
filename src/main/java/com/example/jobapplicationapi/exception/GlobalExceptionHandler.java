package com.example.jobapplicationapi.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
// This class contains exception-handling methods for my REST controllers.
//
// If an exception occurs while processing a REST request,
// Spring looks for a matching @ExceptionHandler method here.
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // If MethodArgumentNotValidException occurs,
    // Spring calls this method and passes the exception object to it.
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            errors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ApiErrorResponse response =
                new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        errors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException exception) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception) {

        // Log the actual exception for debugging.
        // We do not expose its details to the client.
        exception.printStackTrace();

        ApiErrorResponse response =
                new ApiErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal server error"
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Data Conflict"
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}