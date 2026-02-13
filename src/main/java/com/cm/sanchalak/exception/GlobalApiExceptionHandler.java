package com.cm.sanchalak.exception;

import com.cm.sanchalak.dto.ApiError;
import com.cm.sanchalak.dto.ApiResult;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all API endpoints
 * Returns standardized ApiResponse format for consistency across web and mobile
 */
@RestControllerAdvice
public class GlobalApiExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);
    
    /**
     * Validation errors (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        List<String> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        ApiError error = ApiError.of("VALIDATION_ERROR", "Invalid request parameters", errors);
        logger.warn("Validation error: {}", errors);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResult.error(error));
    }
    
    /**
     * Constraint violation (400 Bad Request)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(violation -> violation.getMessage())
            .collect(Collectors.toList());
        
        ApiError error = ApiError.of("CONSTRAINT_VIOLATION", "Validation failed", errors);
        logger.warn("Constraint violation: {}", errors);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResult.error(error));
    }
    
    /**
     * Authentication errors (401 Unauthorized)
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResult<Void>> handleAuthenticationException(
            Exception ex, WebRequest request) {
        
        ApiError error = ApiError.of("AUTHENTICATION_FAILED", "Invalid credentials or token");
        logger.warn("Authentication failed: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResult.error(error));
    }
    
    /**
     * Authorization errors (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        ApiError error = ApiError.of("ACCESS_DENIED", "You don't have permission to access this resource");
        logger.warn("Access denied: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResult.error(error));
    }
    
    /**
     * Resource not found (404 Not Found)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleNotFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        
        ApiError error = ApiError.of("NOT_FOUND", "The requested resource was not found");
        logger.warn("Resource not found: {}", ex.getRequestURL());
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResult.error(error));
    }
    
    /**
     * Business logic exceptions (custom application exceptions)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        ApiError error = ApiError.of("INVALID_REQUEST", ex.getMessage());
        logger.warn("Invalid argument: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResult.error(error));
    }
    
    /**
     * Generic server errors (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        ApiError error = ApiError.of("INTERNAL_ERROR", "An unexpected error occurred. Please try again later.");
        logger.error("Unexpected error: ", ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResult.error(error));
    }
}
