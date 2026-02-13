package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity for tracking which users have read which notices
 */
@Entity
@Table(name = "notice_read_status", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notice_id"}),
    indexes = {
        @Index(name = "idx_read_status_user", columnList = "user_id"),
        @Index(name = "idx_read_status_notice", columnList = "notice_id")
    }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeReadStatus extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId; // User who read the notice
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;
    
    @Column(name = "read_at", nullable = false)
    private Instant readAt;
}
