package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;

import java.time.LocalDate;
import java.util.List;

public class BulkMarkAttendanceRequest {
    private String classId;
    private LocalDate date;
    private List<StudentAttendanceStatus> attendances;
    private String markedBy;

    public String getClassId() {
        return classId;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<StudentAttendanceStatus> getAttendances() {
        return attendances;
    }

    public String getMarkedBy() {
        return markedBy;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setAttendances(List<StudentAttendanceStatus> attendances) {
        this.attendances = attendances;
    }

    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    public static class StudentAttendanceStatus {
        private String studentId;
        private AttendanceStatus status;
        private String remarks;

        public String getStudentId() {
            return studentId;
        }

        public AttendanceStatus getStatus() {
            return status;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public void setStatus(AttendanceStatus status) {
            this.status = status;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
