package com.cm.sanchalak.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event published when a student is marked absent
 */
@Data
@AllArgsConstructor
public class AbsenceRecordedEvent {
    private Long studentId;
    private String studentName;
    private String date;
    private Long attendanceId;
}
