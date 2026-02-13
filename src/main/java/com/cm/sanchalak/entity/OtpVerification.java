package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Temporary OTP storage with encryption and expiry.
 * OTPs are valid for 5 minutes and automatically cleaned up.
 */
@Entity
@Table(name = "otp_verifications", indexes = {
    @Index(name = "idx_otp_mobile_number", columnList = "mobile_number"),
    @Index(name = "idx_otp_expiry", columnList = "expires_at"),
    @Index(name = "idx_otp_lookup", columnList = "mobile_number, is_used, expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "mobile_number", nullable = false, length = 15)
    private String mobileNumber;
    
    @Column(name = "otp_code", nullable = false)
    private String otpCode;  // AES-256 encrypted
    
    @Column(nullable = false, length = 20)
    private String purpose = "LOGIN";  // LOGIN, PASSWORD_RESET
    
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean canAttempt() {
        return attemptCount < 5 && !isExpired() && !isUsed;
    }
}
