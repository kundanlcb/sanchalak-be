package com.cm.sanchalak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Attendance summary DTO for student/parent portal
 * Provides overview of attendance metrics
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceSummaryDto {

    @JsonProperty("studentId")
    private Long studentId;

    @JsonProperty("studentID")
    private String studentID;

    private String studentName;

    private String className;

    private int totalDays;

    private int presentDays;

    private Integer absentDays;

    private Integer lateDays;

    private double percentage;

    private String lastAttendanceDate;

    private String lastAttendanceStatus; // PRESENT, ABSENT, LATE
}
