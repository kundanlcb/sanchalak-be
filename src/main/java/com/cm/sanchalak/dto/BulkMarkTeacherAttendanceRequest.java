package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BulkMarkTeacherAttendanceRequest {
    private LocalDate date;
    private String markedBy;
    private List<TeacherAttendanceStatus> attendances;

    @Data
    public static class TeacherAttendanceStatus {
        private Long teacherId;
        private AttendanceStatus status;
        private String remarks;
    }
}
