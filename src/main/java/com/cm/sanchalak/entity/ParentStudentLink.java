package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Many-to-many relationship between Parent and Student
 * Tracks relationship type (Father, Mother, Guardian) and active status
 */
@Entity
@Table(name = "parent_student_links", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_parent_student", columnNames = {"parent_id", "student_id"})
       },
       indexes = {
    @Index(name = "idx_parent_student_link_parent", columnList = "parent_id"),
    @Index(name = "idx_parent_student_link_student", columnList = "student_id"),
    @Index(name = "idx_parent_student_link_active", columnList = "is_active"),
    @Index(name = "idx_parent_student_link_composite", columnList = "parent_id, student_id, is_active")
})
public class ParentStudentLink {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_parent_link_parent"))
    private Parent parent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_parent_link_student"))
    private Student student;
    
    @Size(max = 20)
    @Column(name = "relationship_type", length = 20)
    private String relationshipType;  // FATHER, MOTHER, GUARDIAN
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "effective_date")
    private LocalDate effectiveDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (effectiveDate == null) {
            effectiveDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public ParentStudentLink() {}

    public ParentStudentLink(Long id, Parent parent, Student student, String relationshipType, Boolean isPrimary, Boolean isActive, LocalDate effectiveDate, LocalDate endDate) {
        this.id = id;
        this.parent = parent;
        this.student = student;
        this.relationshipType = relationshipType;
        this.isPrimary = isPrimary;
        this.isActive = isActive;
        this.effectiveDate = effectiveDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Parent getParent() { return parent; }
    public void setParent(Parent parent) { this.parent = parent; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
