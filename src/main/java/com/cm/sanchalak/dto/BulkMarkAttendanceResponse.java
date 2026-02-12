package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkMarkAttendanceResponse {
    private boolean success;
    private int markedCount;
    private int failedCount;
    private String message;
}
