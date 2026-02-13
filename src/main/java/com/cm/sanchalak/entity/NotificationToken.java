package com.cm.sanchalak.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Entity for storing device notification tokens (FCM/APNs)
 */
@Entity
@Table(name = "notification_tokens", indexes = {
    @Index(name = "idx_token_user", columnList = "user_id"),
    @Index(name = "idx_token_active", columnList = "is_active"),
    @Index(name = "idx_token_value", columnList = "token", unique = true)
})
public class NotificationToken extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String tokenValue; // FCM token or APNs token
    
    @Column(name = "platform", nullable = false, length = 20)
    private String platform; // FCM, APNS
    
    @Column(name = "device_type", length = 50)
    private String deviceType; // ANDROID, IOS
    
    @Column(name = "device_id", length = 100)
    private String deviceId; // Unique device identifier
    
    @Column(name = "app_version", length = 20)
    private String appVersion;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "last_used_at")
    private Instant lastUsedAt;
    
    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTokenValue() { return tokenValue; }
    public void setTokenValue(String tokenValue) { this.tokenValue = tokenValue; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public Instant getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }
}
