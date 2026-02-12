package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "exam_schedules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"exam_term_id", "class_id", "subject_id"})
})
public class ExamSchedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_term_id", nullable = false)
    private ExamTerm examTerm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private Class studentClass;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Column(name = "exam_date")
    private LocalDate examDate;

    public ExamSchedule() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ExamTerm getExamTerm() { return examTerm; }
    public void setExamTerm(ExamTerm examTerm) { this.examTerm = examTerm; }
    public Class getStudentClass() { return studentClass; }
    public void setStudentClass(Class studentClass) { this.studentClass = studentClass; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
}
