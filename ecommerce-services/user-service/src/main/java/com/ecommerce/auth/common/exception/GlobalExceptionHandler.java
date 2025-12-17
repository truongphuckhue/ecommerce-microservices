package com.ecommerce.auth.common.exception;

import com.ecommerce.auth.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {

        log.error("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getData()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        if (ex.getResourceName() != null) {
            details.put("resourceName", ex.getResourceName());
            details.put("fieldName", ex.getFieldName());
            details.put("fieldValue", ex.getFieldValue());
        }

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "RESOURCE_NOT_FOUND",
                details.isEmpty() ? null : details
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        log.error("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Object> response = ApiResponse.validationError(errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error occurred: ", ex);

        ApiResponse<Object> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        log.error("Invalid argument: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "INVALID_ARGUMENT"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        log.error("Illegal state: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "ILLEGAL_STATE"
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}