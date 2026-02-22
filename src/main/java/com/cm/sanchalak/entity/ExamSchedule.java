package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "exam_schedules", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "exam_term_id", "class_id", "subject_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSchedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private UUID schoolId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_term_id", nullable = false)
    private ExamTerm examTerm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass studentClass;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Column(name = "passing_marks")
    private Integer passingMarks;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;
}
