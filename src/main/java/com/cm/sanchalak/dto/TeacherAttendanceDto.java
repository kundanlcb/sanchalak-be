package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAttendanceDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private LocalDate date;
    private AttendanceStatus status;
    private String remarks;
    private String markedBy;
    private LocalDateTime markedDate;
    private boolean isModified;
}
