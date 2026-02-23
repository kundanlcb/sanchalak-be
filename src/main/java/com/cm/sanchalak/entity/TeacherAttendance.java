package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "teacher_attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "teacher_id", "date" })
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAttendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    private String remarks;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @CreatedBy
    @Column(name = "marked_by", updatable = false)
    private String markedBy;

    @LastModifiedBy
    @Column(name = "modified_by")
    private String modifiedBy;

    @Builder.Default
    @Column(name = "is_modified", nullable = false)
    private boolean isModified = false;
}
