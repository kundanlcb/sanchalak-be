package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "homework")
public class Homework extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass studentClass;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    public Homework() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SchoolClass getStudentClass() { return studentClass; }
    public void setStudentClass(SchoolClass studentClass) { this.studentClass = studentClass; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
}
