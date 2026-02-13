package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    
    /**
     * Find valid (unused and not expired) OTP for mobile number
     */
    Optional<OtpVerification> findByMobileNumberAndIsUsedFalseAndExpiresAtAfter(
        String mobileNumber, 
        LocalDateTime currentTime
    );
    
    /**
     * Find latest OTP for mobile number (for rate limiting check)
     */
    Optional<OtpVerification> findTopByMobileNumberOrderByCreatedAtDesc(String mobileNumber);
    
    /**
     * Count OTPs created within time window (for rate limiting)
     */
    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE o.mobileNumber = :mobileNumber " +
           "AND o.createdAt > :since")
    long countRecentOtps(@Param("mobileNumber") String mobileNumber, @Param("since") LocalDateTime since);
    
    /**
     * Cleanup expired OTPs (scheduled job)
     */
    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :cutoffTime")
    int deleteExpiredOtps(@Param("cutoffTime") LocalDateTime cutoffTime);
}
