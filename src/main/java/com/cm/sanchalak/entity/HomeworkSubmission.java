package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for homework submission tracking
 * Stores file URLs as JSON array for multiple file support
 */
@Entity
@Table(name = "homework_submissions", indexes = {
    @Index(name = "idx_homework_submission_homework", columnList = "homework_id"),
    @Index(name = "idx_homework_submission_student", columnList = "student_id"),
    @Index(name = "idx_homework_submission_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class HomeworkSubmission extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;
    
    @Column(name = "is_late", nullable = false)
    private Boolean isLate = false;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;
    
    @Column(name = "submission_file_urls", columnDefinition = "JSON")
    @Convert(converter = JsonStringListConverter.class)
    private List<String> submissionFileUrls = new ArrayList<>();
    
    @Column(name = "student_remarks", columnDefinition = "TEXT")
    private String studentRemarks;
    
    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;
    
    @Column(name = "grade", length = 10)
    private String grade;
    
    @Column(name = "marks_obtained")
    private Double marksObtained;
    
    @Column(name = "graded_at")
    private Instant gradedAt;
    
    @ManyToOne
    @JoinColumn(name = "graded_by")
    private Teacher gradedBy;
    
    public enum SubmissionStatus {
        SUBMITTED,
        RESUBMITTED,
        GRADED,
        RETURNED
    }
}
