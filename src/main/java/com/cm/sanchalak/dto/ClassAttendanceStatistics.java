package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassAttendanceStatistics {
    private Long classId;
    private double averagePercentage;
    private int totalWorkingDays;
    private LocalDate startDate;
    private LocalDate endDate;
}
