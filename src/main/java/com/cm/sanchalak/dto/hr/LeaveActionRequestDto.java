package com.cm.sanchalak.dto.hr;

import com.cm.sanchalak.entity.hr.LeaveStatus;
import lombok.Data;

@Data
public class LeaveActionRequestDto {
    private LeaveStatus status; // APPROVED, REJECTED
    private String comments;
}
