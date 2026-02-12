package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceRecordDto {
    private Long id;
    private Long studentId;
    private Long classId;
    private LocalDate date;
    private AttendanceStatus status;
    private String remarks;
    private String markedBy;
    private LocalDateTime markedDate;
    private boolean isModified;
}
