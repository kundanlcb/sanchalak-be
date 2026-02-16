package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Many-to-many relationship between Parent and Student
 * Tracks relationship type (Father, Mother, Guardian) and active status
 */
@Entity
@Table(name = "parent_student_links", uniqueConstraints = {
        @UniqueConstraint(name = "uk_parent_student", columnNames = { "parent_id", "student_id" })
}, indexes = {
        @Index(name = "idx_parent_student_link_parent", columnList = "parent_id"),
        @Index(name = "idx_parent_student_link_student", columnList = "student_id"),
        @Index(name = "idx_parent_student_link_active", columnList = "is_active"),
        @Index(name = "idx_parent_student_link_composite", columnList = "parent_id, student_id, is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentStudentLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false, foreignKey = @ForeignKey(name = "fk_parent_link_parent"))
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_parent_link_student"))
    private Student student;

    @Size(max = 20)
    @Column(name = "relationship_type", length = 20)
    private String relationshipType; // FATHER, MOTHER, GUARDIAN

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (effectiveDate == null) {
            effectiveDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
