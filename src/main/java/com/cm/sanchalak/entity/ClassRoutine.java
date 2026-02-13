package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "class_routines", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"class_id", "day_of_week", "period"}),
    @UniqueConstraint(columnNames = {"teacher_id", "day_of_week", "period"})
})
public class ClassRoutine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @NotNull
    @Column(nullable = false)
    private Integer period; // 1-8

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass studentClass;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public SchoolClass getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(SchoolClass studentClass) {
        this.studentClass = studentClass;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
}
