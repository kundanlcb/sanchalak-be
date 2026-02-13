package com.cm.sanchalak.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Entity for logging sent notifications
 */
@Entity
@Table(name = "notification_logs", indexes = {
    @Index(name = "idx_log_user", columnList = "user_id"),
    @Index(name = "idx_log_type", columnList = "notification_type"),
    @Index(name = "idx_log_sent_at", columnList = "sent_at DESC"),
    @Index(name = "idx_log_status", columnList = "delivery_status")
})
public class NotificationLog extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType; // ABSENCE, FEE_DUE, NOTICE, BUS_ALERT, HOMEWORK_DUE
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String body;
    
    @Column(name = "data_payload", columnDefinition = "TEXT")
    private String dataPayload; // JSON string with additional data
    
    @Column(name = "target_token", length = 500)
    private String targetToken; // FCM/APNs token used
    
    @Column(name = "platform", length = 20)
    private String platform; // FCM, APNS
    
    @Column(name = "delivery_status", nullable = false, length = 20)
    private String deliveryStatus; // SENT, DELIVERED, FAILED
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
    
    @Column(name = "delivered_at")
    private Instant deliveredAt;
    
    @Column(name = "reference_id", length = 100)
    private String messageId; // FCM message ID or related entity ID
    
    @Column(name = "reference_type", length = 50)
    private String referenceType; // ATTENDANCE, FEE, NOTICE, TRANSPORT

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getDataPayload() { return dataPayload; }
    public void setDataPayload(String dataPayload) { this.dataPayload = dataPayload; }

    public String getTargetToken() { return targetToken; }
    public void setTargetToken(String targetToken) { this.targetToken = targetToken; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public void setTargetPlatform(String platform) { this.platform = platform; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
}
