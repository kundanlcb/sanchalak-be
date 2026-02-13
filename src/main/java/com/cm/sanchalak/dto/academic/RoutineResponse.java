package com.cm.sanchalak.dto.academic;

import com.cm.sanchalak.entity.ClassRoutine;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class RoutineResponse {
    private Long id;
    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private DayOfWeek dayOfWeek;
    private Integer period;
    private LocalTime startTime;
    private LocalTime endTime;

    public RoutineResponse() {}

    public RoutineResponse(ClassRoutine routine) {
        this.id = routine.getId();
        this.classId = routine.getStudentClass().getId();
        this.className = routine.getStudentClass().getName();
        this.subjectId = routine.getSubject().getId();
        this.subjectName = routine.getSubject().getName();
        this.teacherId = routine.getTeacher().getId();
        this.teacherName = routine.getTeacher().getName();
        this.dayOfWeek = routine.getDayOfWeek();
        this.period = routine.getPeriod();
        this.startTime = routine.getStartTime();
        this.endTime = routine.getEndTime();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getPeriod() { return period; }
    public void setPeriod(Integer period) { this.period = period; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
