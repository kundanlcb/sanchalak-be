package com.cm.sanchalak.service.hr;

import com.cm.sanchalak.dto.hr.LeaveBalanceDto;
import com.cm.sanchalak.dto.hr.LeaveTypeDto;

import java.util.List;

public interface LeavePolicyService {
    List<LeaveTypeDto> getAllLeaveTypes();

    LeaveTypeDto createLeaveType(LeaveTypeDto dto);

    LeaveTypeDto updateLeaveType(Long id, LeaveTypeDto dto);

    void deleteLeaveType(Long id);

    // Initialize standard leave quotas for all teachers for the academic year
    void initializeTeacherBalances(String academicYear);

    // Get leave balances for a specific user (targetUserId)
    List<LeaveBalanceDto> getLeaveBalances(Long targetUserId, String academicYear);
}
