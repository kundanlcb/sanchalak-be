package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh tokens for JWT authentication with family tracking for theft detection.
 * Tokens are one-time use with automatic rotation.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token_hash", columnList = "token_hash", unique = true),
    @Index(name = "idx_refresh_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_expiry", columnList = "expires_at"),
    @Index(name = "idx_refresh_family_id", columnList = "token_family_id"),
    @Index(name = "idx_refresh_active", columnList = "user_id, is_revoked, expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, 
                columnDefinition = "BINARY(16)",
                foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private User user;
    
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;  // BCrypt hashed
    
    @Column(name = "device_id", length = 100)
    private String deviceId;
    
    @Column(name = "device_type", length = 20)
    private String deviceType;  // ANDROID, IOS, WEB
    
    @Column(name = "token_family_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID tokenFamilyId;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (tokenFamilyId == null) {
            tokenFamilyId = UUID.randomUUID();
        }
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !isRevoked && !isExpired();
    }
    
    public void revoke() {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
    }
}
