package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_marks", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "student_id", "exam_schedule_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentMarks extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_schedule_id", nullable = false)
    private ExamSchedule examSchedule;

    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;

    @Column(name = "remarks")
    private String remarks;
}
