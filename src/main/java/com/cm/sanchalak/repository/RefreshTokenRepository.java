package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.RefreshToken;
import com.cm.sanchalak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find refresh token by hashed value
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    /**
     * Find all active tokens for a user
     */
    List<RefreshToken> findByUserAndIsRevokedFalseAndExpiresAtAfter(
        User user, 
        LocalDateTime currentTime
    );
    
    /**
     * Find all tokens in a token family (for theft detection)
     */
    List<RefreshToken> findByTokenFamilyId(UUID tokenFamilyId);
    
    /**
     * Revoke all tokens for a user (logout all devices)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt " +
           "WHERE rt.user = :user AND rt.isRevoked = false")
    int revokeAllUserTokens(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);
    
    /**
     * Revoke all tokens in a family (reuse detected)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt " +
           "WHERE rt.tokenFamilyId = :familyId AND rt.isRevoked = false")
    int revokeTokenFamily(@Param("familyId") UUID familyId, @Param("revokedAt") LocalDateTime revokedAt);
    
    /**
     * Cleanup expired tokens (scheduled job)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoffTime")
    int deleteExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);
}
