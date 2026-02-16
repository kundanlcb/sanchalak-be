package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkMarkAttendanceRequest {
    @NotNull(message = "Class ID is required")
    private Long classId;
    private LocalDate date;
    private List<StudentAttendanceStatus> attendances;
    private String markedBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentAttendanceStatus {
        @NotNull(message = "Student ID is required")
        private Long studentId;
        private AttendanceStatus status;
        private String remarks;
    }
}
