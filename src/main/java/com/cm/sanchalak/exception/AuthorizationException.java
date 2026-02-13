package com.cm.sanchalak.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for authorization failures
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AuthorizationException extends RuntimeException {
    private String errorCode;
    
    public AuthorizationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public AuthorizationException(String message) {
        super(message);
        this.errorCode = "AUTHZ_ERROR";
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
