package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "attendance_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "date"})
})
@EntityListeners(AuditingEntityListener.class)
public class AttendanceRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class schoolClass;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    private String remarks;

    @CreatedBy
    @Column(name = "marked_by", updatable = false)
    // nullable = false usually but depends on if we have auditor aware set up correctly, 
    // for now let's leave nullable true or handle via validation
    private String markedBy;

    @LastModifiedBy
    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "is_modified", nullable = false)
    private boolean isModified = false;
}
