package com.cm.sanchalak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for OTP verification
 */
public record OtpVerifyDto(
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    String mobileNumber,
    
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    String otp,
    
    String deviceId,
    
    String deviceType  // ANDROID, IOS
) {}
