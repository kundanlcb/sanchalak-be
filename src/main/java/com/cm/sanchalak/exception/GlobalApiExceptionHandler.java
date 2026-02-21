package com.cm.sanchalak.exception;

import com.cm.sanchalak.dto.ApiError;
import com.cm.sanchalak.dto.ApiResult;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all API endpoints
 * Returns standardized ApiResponse format for consistency across web and mobile
 */
@RestControllerAdvice
public class GlobalApiExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

        // ==========================================
        // 1. Validation and Request Binding Errors (400)
        // ==========================================

        @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
        public ResponseEntity<ApiResult<Void>> handleValidationException(
                        Exception ex, WebRequest request) {

                List<FieldError> fieldErrors;
                if (ex instanceof MethodArgumentNotValidException manve) {
                        fieldErrors = manve.getBindingResult().getFieldErrors();
                } else {
                        fieldErrors = ((BindException) ex).getBindingResult().getFieldErrors();
                }

                Map<String, String> errorsMap = new HashMap<>();
                for (FieldError error : fieldErrors) {
                        errorsMap.put(error.getField(), error.getDefaultMessage());
                }

                List<String> errorMessages = fieldErrors.stream()
                                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                                .collect(Collectors.toList());

                ApiError error = ApiError.of("VALIDATION_ERROR", "Invalid request parameters", errorMessages);
                logger.warn("Validation error: {}", errorsMap);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResult.error(error));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResult<Void>> handleConstraintViolation(
                        ConstraintViolationException ex, WebRequest request) {

                List<String> errors = ex.getConstraintViolations()
                                .stream()
                                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                                .collect(Collectors.toList());

                ApiError error = ApiError.of("CONSTRAINT_VIOLATION", "Validation failed", errors);
                logger.warn("Constraint violation: {}", errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResult.error(error));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResult<Void>> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex, WebRequest request) {

                ApiError error = ApiError.of("MALFORMED_REQUEST", "Malformed JSON request or invalid data type");
                logger.warn("Malformed JSON request: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.error(error));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResult<Void>> handleMethodArgumentTypeMismatch(
                        MethodArgumentTypeMismatchException ex, WebRequest request) {

                String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
                String message = String.format("Parameter '%s' should be of type '%s'", ex.getName(), typeName);

                ApiError error = ApiError.of("TYPE_MISMATCH", message);
                logger.warn("Type mismatch: {}", message);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.error(error));
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiResult<Void>> handleMissingServletRequestParameter(
                        MissingServletRequestParameterException ex, WebRequest request) {

                String message = String.format("Required request parameter '%s' is missing", ex.getParameterName());
                ApiError error = ApiError.of("MISSING_PARAMETER", message);
                logger.warn("Missing parameter: {}", message);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.error(error));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResult<Void>> handleIllegalArgumentException(
                        IllegalArgumentException ex, WebRequest request) {

                ApiError error = ApiError.of("INVALID_REQUEST", ex.getMessage());
                logger.warn("Invalid argument: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResult.error(error));
        }

        // ==========================================
        // 2. Routing and HTTP Protocol Errors (404, 405, 415)
        // ==========================================

        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<ApiResult<Void>> handleNotFoundException(
                        NoHandlerFoundException ex, WebRequest request) {

                ApiError error = ApiError.of("NOT_FOUND",
                                "The requested endpoint was not found: " + ex.getRequestURL());
                logger.warn("Resource not found: {}", ex.getRequestURL());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResult.error(error));
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiResult<Void>> handleHttpRequestMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex, WebRequest request) {

                ApiError error = ApiError.of("METHOD_NOT_ALLOWED", "HTTP method not supported: " + ex.getMethod());
                logger.warn("Method not allowed: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResult.error(error));
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ApiResult<Void>> handleHttpMediaTypeNotSupported(
                        HttpMediaTypeNotSupportedException ex, WebRequest request) {

                ApiError error = ApiError.of("UNSUPPORTED_MEDIA_TYPE",
                                "Media type not supported: " + ex.getContentType());
                logger.warn("Unsupported media type: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ApiResult.error(error));
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResult<Void>> handleResourceNotFoundException(
                        ResourceNotFoundException ex, WebRequest request) {

                ApiError error = ApiError.of("NOT_FOUND", ex.getMessage());
                logger.warn("Resource not found: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResult.error(error));
        }

        // ==========================================
        // 3. Security and Authentication (401, 403)
        // ==========================================

        @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
        public ResponseEntity<ApiResult<Void>> handleAuthenticationException(
                        Exception ex, WebRequest request) {

                ApiError error = ApiError.of("AUTHENTICATION_FAILED", "Invalid credentials or token");
                logger.warn("Authentication failed: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResult.error(error));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResult<Void>> handleAccessDeniedException(
                        AccessDeniedException ex, WebRequest request) {

                ApiError error = ApiError.of("ACCESS_DENIED", "You don't have permission to access this resource");
                logger.warn("Access denied: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResult.error(error));
        }

        // ==========================================
        // 4. Database, Concurrency and Data Integrity (409)
        // ==========================================

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResult<Void>> handleDataIntegrityException(
                        DataIntegrityViolationException ex, WebRequest request) {

                String rootMsg = ex.getRootCause() != null ? ex.getRootCause().getMessage().toLowerCase()
                                : ex.getMessage().toLowerCase();

                String cleanMessage = "Data integrity conflict occurred.";
                if (rootMsg.contains("duplicate entry")) {
                        cleanMessage = "A duplicate record already exists.";
                } else if (rootMsg.contains("foreign key constraint")) {
                        cleanMessage = "Cannot proceed because a related record is missing or currently in use.";
                } else if (rootMsg.contains("not null constraint")) {
                        cleanMessage = "A required database field is missing.";
                }

                ApiError error = ApiError.of("DATA_CONFLICT", cleanMessage);
                // Log the actual DB error internally for debugging, but don't leak it
                logger.error("Data integrity violation: {}", rootMsg);

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ApiResult.error(error));
        }

        @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
        public ResponseEntity<ApiResult<Void>> handleHibernateConstraintViolation(
                        org.hibernate.exception.ConstraintViolationException ex, WebRequest request) {

                ApiError error = ApiError.of("DATA_CONFLICT", "A database constraint was violated.");
                logger.error("Hibernate constraint violation: {}", ex.getSQLException().getMessage());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ApiResult.error(error));
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiResult<Void>> handleDuplicateResourceException(
                        DuplicateResourceException ex, WebRequest request) {

                ApiError error = ApiError.of("DUPLICATE_RESOURCE", ex.getMessage());
                logger.warn("Duplicate resource: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ApiResult.error(error));
        }

        @ExceptionHandler(OptimisticLockingFailureException.class)
        public ResponseEntity<ApiResult<Void>> handleOptimisticLockingFailure(
                        OptimisticLockingFailureException ex, WebRequest request) {

                ApiError error = ApiError.of("CONCURRENT_MODIFICATION",
                                "The record was modified by another user. Please refresh and try again.");
                logger.warn("Optimistic locking failure: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResult.error(error));
        }

        // ==========================================
        // 5. Application General and Fallbacks (400, 500)
        // ==========================================

        @ExceptionHandler(AppException.class)
        public ResponseEntity<ApiResult<Void>> handleAppException(
                        AppException ex, WebRequest request) {

                ApiError error = ApiError.of("APP_ERROR", ex.getMessage());
                logger.warn("Application error: {}", ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResult.error(error));
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResult<Void>> handleRuntimeException(
                        RuntimeException ex, WebRequest request) {

                // Check for specific messages to map to 404
                if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
                        ApiError error = ApiError.of("NOT_FOUND", ex.getMessage());
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(ApiResult.error(error));
                }

                ApiError error = ApiError.of("INTERNAL_SERVER_ERROR", "An unexpected server error occurred.");
                logger.error("Runtime error: ", ex); // Logs full stack trace internally

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResult.error(error));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResult<Void>> handleGlobalException(
                        Exception ex, WebRequest request) {

                ApiError error = ApiError.of("INTERNAL_SERVER_ERROR",
                                "An unexpected error occurred. Please try again later.");
                logger.error("Unexpected error: ", ex); // Logs full stack trace internally

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResult.error(error));
        }
}
