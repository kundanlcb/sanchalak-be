package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BulkMarkAttendanceRequest {
    private Long classId;
    private LocalDate date;
    private List<StudentAttendanceStatus> attendances;
    private String markedBy;

    @Data
    public static class StudentAttendanceStatus {
        private Long studentId;
        private AttendanceStatus status;
        private String remarks;
    }
}
