package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.OtpVerification;
import com.cm.sanchalak.repository.OtpVerificationRepository;
import com.cm.sanchalak.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for OTP generation, encryption, validation, and rate limiting
 */
@Service
public class OtpService {
    
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private final OtpVerificationRepository otpRepository;
    private final AuditLogService auditLogService;
    
    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    
    @Value("${app.otp.encryption-key}")
    private String encryptionKey;
    
    @Value("${app.otp.rate-limit.max-requests:3}")
    private int maxOtpRequests;
    
    @Value("${app.otp.rate-limit.window-minutes:15}")
    private int rateLimitWindowMinutes;
    
    public OtpService(OtpVerificationRepository otpRepository, AuditLogService auditLogService) {
        this.otpRepository = otpRepository;
        this.auditLogService = auditLogService;
    }
    
    /**
     * Generate and send OTP for mobile number
     */
    @Transactional
    public String generateOtp(String mobileNumber) {
        // Check rate limiting
        checkRateLimit(mobileNumber);
        
        // Generate 6-digit OTP
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        
        // Encrypt OTP before storage
        String encryptedOtp = encryptOtp(otp);
        
        // Save OTP verification record
        OtpVerification verification = new OtpVerification();
        verification.setMobileNumber(mobileNumber);
        verification.setOtpCode(encryptedOtp);
        verification.setPurpose("LOGIN");
        verification.setAttemptCount(0);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        verification.setIsUsed(false);
        
        otpRepository.save(verification);
        
        logger.info("OTP generated for mobile: {}", maskMobileNumber(mobileNumber));
        
        // Log generation attempt
        auditLogService.logAction(null, "OTP_GENERATED", "MOBILE_NUMBER", mobileNumber, "OTP generated for login.", null, null, "SUCCESS");
        
        // In production, integrate with SMS gateway here
        // For development, return the OTP (remove this in production)
        return otp;
    }
    
    /**
     * Validate OTP for mobile number
     */
    @Transactional
    public boolean validateOtp(String mobileNumber, String otp) {
        Optional<OtpVerification> verificationOpt = otpRepository
            .findByMobileNumberAndIsUsedFalseAndExpiresAtAfter(mobileNumber, LocalDateTime.now());
        
        if (verificationOpt.isEmpty()) {
            logger.warn("No valid OTP found for mobile: {}", maskMobileNumber(mobileNumber));
            auditLogService.logAction(null, "OTP_FAILED", "MOBILE_NUMBER", mobileNumber, "No valid OTP found.", null, null, "FAILURE");
            return false;
        }
        
        OtpVerification verification = verificationOpt.get();
        
        // Check attempt count
        if (verification.getAttemptCount() >= 5) {
            logger.warn("Maximum OTP attempts exceeded for mobile: {}", maskMobileNumber(mobileNumber));
            auditLogService.logAction(null, "OTP_LOCKED", "MOBILE_NUMBER", mobileNumber, "Max OTP attempts exceeded.", null, null, "FAILURE");
            return false;
        }
        
        // Increment attempt count
        verification.setAttemptCount(verification.getAttemptCount() + 1);
        otpRepository.save(verification);
        
        // Decrypt and compare
        String decryptedOtp = decryptOtp(verification.getOtpCode());
        if (decryptedOtp.equals(otp)) {
            verification.setIsUsed(true);
            verification.setUsedAt(LocalDateTime.now());
            otpRepository.save(verification);
            
            logger.info("OTP validated successfully for mobile: {}", maskMobileNumber(mobileNumber));
            auditLogService.logAction(null, "OTP_VERIFIED", "MOBILE_NUMBER", mobileNumber, "OTP verification successful.", null, null, "SUCCESS");
            return true;
        }
        
        logger.warn("Invalid OTP provided for mobile: {}", maskMobileNumber(mobileNumber));
        auditLogService.logAction(null, "OTP_FAILED", "MOBILE_NUMBER", mobileNumber, "OTP verification failed.", null, null, "FAILURE");
        return false;
    }
    
    /**
     * Check rate limiting for OTP requests
     */
    private void checkRateLimit(String mobileNumber) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(rateLimitWindowMinutes);
        long recentCount = otpRepository.countRecentOtps(mobileNumber, windowStart);
        
        if (recentCount >= maxOtpRequests) {
            throw new IllegalStateException(
                String.format("Too many OTP requests. Please try again after %d minutes.", 
                    rateLimitWindowMinutes)
            );
        }
    }
    
    /**
     * Encrypt OTP using AES-256
     */
    private String encryptOtp(String otp) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptionKey.getBytes(StandardCharsets.UTF_8), 
                "AES"
            );
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(otp.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.error("Failed to encrypt OTP", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypt OTP using AES-256
     */
    private String decryptOtp(String encryptedOtp) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptionKey.getBytes(StandardCharsets.UTF_8), 
                "AES"
            );
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedOtp));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Failed to decrypt OTP", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    /**
     * Cleanup expired OTPs (scheduled job)
     */
    @Transactional
    public int cleanupExpiredOtps() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        int deleted = otpRepository.deleteExpiredOtps(cutoffTime);
        logger.info("Cleaned up {} expired OTP records", deleted);
        return deleted;
    }
    
    /**
     * Mask mobile number for logging (e.g., 9876543210 -> 987***3210)
     */
    private String maskMobileNumber(String mobile) {
        if (mobile == null || mobile.length() < 6) {
            return "***";
        }
        return mobile.substring(0, 3) + "***" + mobile.substring(mobile.length() - 4);
    }
}
