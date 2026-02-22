package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity for notices/announcements
 */
@Entity
@Table(name = "notices", indexes = {
        @Index(name = "idx_notice_target_role", columnList = "target_role"),
        @Index(name = "idx_notice_publish_date", columnList = "publish_date DESC"),
        @Index(name = "idx_notice_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private UUID schoolId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "target_role", nullable = false, length = 20)
    private String targetRole; // PARENT, STUDENT, TEACHER, ALL

    @Column(name = "publish_date", nullable = false)
    private LocalDate publishDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy; // Teacher/Admin who created the notice

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl; // Optional file attachment
}
