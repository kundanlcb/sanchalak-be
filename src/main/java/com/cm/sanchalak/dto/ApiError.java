package com.cm.sanchalak.dto;

import java.util.List;

/**
 * Standardized error object for mobile API responses
 */
public class ApiError {
    
    private String code;
    private String message;
    private List<String> details;

    public ApiError() {}

    public ApiError(String code, String message, List<String> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public static ApiErrorBuilder builder() {
        return new ApiErrorBuilder();
    }

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }
    
    public static ApiError of(String code, String message, List<String> details) {
        return new ApiError(code, message, details);
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public List<String> getDetails() { return details; }

    public void setCode(String code) { this.code = code; }
    public void setMessage(String message) { this.message = message; }
    public void setDetails(List<String> details) { this.details = details; }

    public static class ApiErrorBuilder {
        private String code;
        private String message;
        private List<String> details;

        ApiErrorBuilder() {}

        public ApiErrorBuilder code(String code) {
            this.code = code;
            return this;
        }

        public ApiErrorBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ApiErrorBuilder details(List<String> details) {
            this.details = details;
            return this;
        }

        public ApiError build() {
            return new ApiError(code, message, details);
        }
    }
}
