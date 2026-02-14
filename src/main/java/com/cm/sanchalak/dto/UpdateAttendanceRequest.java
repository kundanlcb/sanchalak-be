package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;
import lombok.Data;

@Data
public class UpdateAttendanceRequest {
    private AttendanceStatus status;
    private String remarks;
}
