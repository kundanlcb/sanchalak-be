package com.cm.sanchalak.dto.hr;

import lombok.Data;

@Data
public class LeaveBalanceDto {
    private Long id;
    private Long targetUserId;
    private LeaveTypeDto leaveType;
    private String academicYear;
    private double totalGranted;
    private double totalUsed;
    private double balance;
}
