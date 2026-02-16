package com.cm.sanchalak.dto.academic;

import com.cm.sanchalak.entity.ClassRoutine;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class RoutineResponse {
    private Long id;
    @JsonProperty("classId")
    private Long classId;
    @JsonProperty("classID")
    private String classID;
    private String className;

    @JsonProperty("subjectId")
    private Long subjectId;
    @JsonProperty("subjectID")
    private String subjectID;
    private String subjectName;

    @JsonProperty("teacherId")
    private Long teacherId;
    @JsonProperty("teacherID")
    private String teacherID;
    private String teacherName;

    private DayOfWeek dayOfWeek;
    private Integer period;
    private LocalTime startTime;
    private LocalTime endTime;

    public RoutineResponse() {
    }

    public RoutineResponse(ClassRoutine routine) {
        this.id = routine.getId();
        this.classId = routine.getStudentClass().getId();
        this.classID = "CLS-01-" + routine.getStudentClass().getId();
        this.className = routine.getStudentClass().getName();
        this.subjectId = routine.getSubject().getId();
        this.subjectID = "SUB-" + routine.getSubject().getId();
        this.subjectName = routine.getSubject().getName();
        this.teacherId = routine.getTeacher().getId();
        this.teacherID = "TCH-" + routine.getTeacher().getId();
        this.teacherName = routine.getTeacher().getName();
        this.dayOfWeek = routine.getDayOfWeek();
        this.period = routine.getPeriod();
        this.startTime = routine.getStartTime();
        this.endTime = routine.getEndTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(String teacherID) {
        this.teacherID = teacherID;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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
}
