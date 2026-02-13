package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity for audit logs of security and critical actions
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action_type"),
    @Index(name = "idx_audit_created_at", columnList = "created_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId; // Nullable if action is by unauthenticated user
    
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // AUTH_SUCCESS, AUTH_FAILURE, PAYMENT_INIT, PAYMENT_SUCCESS, DATA_ACCESS
    
    @Column(name = "resource_type", length = 50)
    private String resourceType; // STUDENT, FEE, OTP, PARENT
    
    @Column(name = "resource_id")
    private String resourceId; // ID of the resource accessed
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON or text details
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 200)
    private String userAgent;
    
    @Column(name = "status", length = 20)
    private String status; // SUCCESS, FAILURE
}
