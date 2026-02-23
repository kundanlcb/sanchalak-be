package com.cm.sanchalak.dto.hr;

import com.cm.sanchalak.entity.hr.LeaveStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveRequestDto {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private LeaveTypeDto leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isHalfDay;
    private String reason;
    private String attachmentUrl;
    private LeaveStatus status;
    private Long approverId;
    private String approverComments;
    private LocalDateTime actionedAt;
    private LocalDateTime createdAt;
}
