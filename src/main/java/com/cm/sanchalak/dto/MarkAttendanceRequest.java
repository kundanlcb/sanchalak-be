package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MarkAttendanceRequest {
    private Long studentId;
    private Long classId;
    private LocalDate date;
    private AttendanceStatus status;
    private String remarks;
}
