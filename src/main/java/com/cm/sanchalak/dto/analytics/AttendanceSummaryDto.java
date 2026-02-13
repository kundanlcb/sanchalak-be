package com.cm.sanchalak.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceSummaryDto {
    private int totalDays;
    private int presentDays;
    private double percentage;
}
