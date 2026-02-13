package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.RefreshToken;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for refresh token management with rotation and theft detection
 */
@Service
public class RefreshTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${app.jwt.refresh-token-expiry-days:30}")
    private int refreshTokenExpiryDays;
    
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }
    
    /**
     * Generate new refresh token for user
     */
    @Transactional
    public String createRefreshToken(User user, String deviceId, String deviceType) {
        // Generate random token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        // Hash token before storage
        String tokenHash = passwordEncoder.encode(token);
        
        // Create refresh token entity
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setDeviceId(deviceId);
        refreshToken.setDeviceType(deviceType);
        refreshToken.setTokenFamilyId(UUID.randomUUID());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays));
        refreshToken.setIsRevoked(false);
        
        refreshTokenRepository.save(refreshToken);
        
        logger.info("Created refresh token for user: {} on device: {}", 
            user.getId(), deviceId);
        
        return token;
    }
    
    /**
     * Rotate refresh token (one-time use)
     */
    @Transactional
    public String rotateRefreshToken(String oldToken) {
        // Find token by hash
        Optional<RefreshToken> oldTokenOpt = findByToken(oldToken);
        
        if (oldTokenOpt.isEmpty()) {
            logger.warn("Refresh token not found");
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        RefreshToken oldRefreshToken = oldTokenOpt.get();
        
        // Check if token is valid
        if (!oldRefreshToken.isValid()) {
            logger.warn("Attempted to use invalid refresh token for user: {}", 
                oldRefreshToken.getUser().getId());
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }
        
        // Check for token reuse (theft detection)
        if (oldRefreshToken.getLastUsedAt() != null) {
            logger.error("Token reuse detected! Revoking token family: {}", 
                oldRefreshToken.getTokenFamilyId());
            
            // Revoke entire token family
            refreshTokenRepository.revokeTokenFamily(
                oldRefreshToken.getTokenFamilyId(), 
                LocalDateTime.now()
            );
            
            throw new SecurityException("Token reuse detected. All tokens have been revoked for security.");
        }
        
        // Mark old token as used (one-time use)
        oldRefreshToken.setLastUsedAt(LocalDateTime.now());
        oldRefreshToken.revoke();
        refreshTokenRepository.save(oldRefreshToken);
        
        // Generate new token in same family
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String newToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String newTokenHash = passwordEncoder.encode(newToken);
        
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(oldRefreshToken.getUser());
        newRefreshToken.setTokenHash(newTokenHash);
        newRefreshToken.setDeviceId(oldRefreshToken.getDeviceId());
        newRefreshToken.setDeviceType(oldRefreshToken.getDeviceType());
        newRefreshToken.setTokenFamilyId(oldRefreshToken.getTokenFamilyId());  // Same family
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays));
        newRefreshToken.setIsRevoked(false);
        
        refreshTokenRepository.save(newRefreshToken);
        
        logger.info("Rotated refresh token for user: {}", oldRefreshToken.getUser().getId());
        
        return newToken;
    }
    
    /**
     * Validate refresh token and return user
     */
    @Transactional(readOnly = true)
    public Optional<User> validateToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = findByToken(token);
        
        if (refreshTokenOpt.isEmpty()) {
            return Optional.empty();
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        
        if (!refreshToken.isValid()) {
            return Optional.empty();
        }
        
        return Optional.of(refreshToken.getUser());
    }
    
    /**
     * Find refresh token by raw token value (searches by hash)
     */
    private Optional<RefreshToken> findByToken(String token) {
        List<RefreshToken> allTokens = refreshTokenRepository.findAll();
        
        for (RefreshToken rt : allTokens) {
            if (passwordEncoder.matches(token, rt.getTokenHash())) {
                return Optional.of(rt);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Revoke specific refresh token
     */
    @Transactional
    public void revokeToken(String token) {
        Optional<RefreshToken> tokenOpt = findByToken(token);
        
        if (tokenOpt.isPresent()) {
            RefreshToken refreshToken = tokenOpt.get();
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            
            logger.info("Revoked refresh token for user: {}", refreshToken.getUser().getId());
        }
    }
    
    /**
     * Revoke all tokens for a user (logout all devices)
     */
    @Transactional
    public int revokeAllUserTokens(User user) {
        int count = refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
        logger.info("Revoked {} refresh tokens for user: {}", count, user.getId());
        return count;
    }
    
    /**
     * Cleanup expired tokens (scheduled job)
     */
    @Transactional
    public int cleanupExpiredTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(60);  // Keep 60 days history
        int deleted = refreshTokenRepository.deleteExpiredTokens(cutoffTime);
        logger.info("Cleaned up {} expired refresh tokens", deleted);
        return deleted;
    }
}
