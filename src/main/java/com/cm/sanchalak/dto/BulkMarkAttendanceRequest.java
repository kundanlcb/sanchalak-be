package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;

import java.time.LocalDate;
import java.util.List;

public class BulkMarkAttendanceRequest {
    private Long classId;
    private LocalDate date;
    private List<StudentAttendanceStatus> attendances;
    private String markedBy;

    public Long getClassId() { return classId; }
    public LocalDate getDate() { return date; }
    public List<StudentAttendanceStatus> getAttendances() { return attendances; }
    public String getMarkedBy() { return markedBy; }

    public void setClassId(Long classId) { this.classId = classId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setAttendances(List<StudentAttendanceStatus> attendances) { this.attendances = attendances; }
    public void setMarkedBy(String markedBy) { this.markedBy = markedBy; }

    public static class StudentAttendanceStatus {
        private Long studentId;
        private AttendanceStatus status;
        private String remarks;

        public Long getStudentId() { return studentId; }
        public AttendanceStatus getStatus() { return status; }
        public String getRemarks() { return remarks; }

        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public void setStatus(AttendanceStatus status) { this.status = status; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
}
