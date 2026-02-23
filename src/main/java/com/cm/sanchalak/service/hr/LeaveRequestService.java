package com.cm.sanchalak.service.hr;

import com.cm.sanchalak.dto.hr.LeaveActionRequestDto;
import com.cm.sanchalak.dto.hr.LeaveRequestDto;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequestDto applyLeave(LeaveRequestDto dto);

    List<LeaveRequestDto> getMyRequests();

    List<LeaveRequestDto> getPendingRequests();

    LeaveRequestDto processRequest(Long requestId, LeaveActionRequestDto action);

    void cancelRequest(Long requestId);
}
