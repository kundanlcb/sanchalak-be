package com.cm.sanchalak.exception;

import com.cm.sanchalak.dto.ApiResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class MobileApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError() != null 
            ? ex.getBindingResult().getFieldError().getDefaultMessage() 
            : "Validation error";
        return new ResponseEntity<>(ApiResult.error("VALIDATION_ERROR", errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResult<Void>> handleAuthenticationException(AuthenticationException ex) {
        return new ResponseEntity<>(ApiResult.error("UNAUTHORIZED", ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(ApiResult.error("FORBIDDEN", "Access denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleNotFoundException(NoHandlerFoundException ex) {
        return new ResponseEntity<>(ApiResult.error("NOT_FOUND", "Resource not found"), HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(RuntimeException.class) // Catch generic runtime exceptions (e.g., from checks in code)
    public ResponseEntity<ApiResult<Void>> handleRuntimeException(RuntimeException ex) {
         // You might want to be more specific or log this
        return new ResponseEntity<>(ApiResult.error("INTERNAL_ERROR", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGeneralException(Exception ex) {
        return new ResponseEntity<>(ApiResult.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
