package com.cm.sanchalak.service.hr;

import com.cm.sanchalak.dto.hr.LeaveActionRequestDto;
import com.cm.sanchalak.dto.hr.LeaveRequestDto;
import com.cm.sanchalak.dto.hr.LeaveTypeDto;
import com.cm.sanchalak.entity.AttendanceStatus;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.entity.hr.LeaveBalance;
import com.cm.sanchalak.entity.hr.LeaveRequest;
import com.cm.sanchalak.entity.hr.LeaveStatus;
import com.cm.sanchalak.entity.hr.LeaveType;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.hr.LeaveBalanceRepository;
import com.cm.sanchalak.repository.hr.LeaveRequestRepository;
import com.cm.sanchalak.repository.hr.LeaveTypeRepository;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.service.NotificationService;
import com.cm.sanchalak.service.TeacherAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherAttendanceService teacherAttendanceService;
    private final NotificationService notificationService;

    private String getTenantId() {
        return SchoolContext.getSchoolId().toString();
    }

    private Long getCurrentUserId() {
        return 1L; // Placeholder
    }

    @Override
    @Transactional
    public LeaveRequestDto applyLeave(LeaveRequestDto dto) {
        String tenantId = getTenantId();
        Long requesterId = dto.getRequesterId();

        List<LeaveRequest> overlaps = leaveRequestRepository.findOverlappingRequests(
                tenantId, requesterId, dto.getStartDate(), dto.getEndDate());
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException("Overlap found with an existing leave request.");
        }

        LeaveType leaveType = leaveTypeRepository.findByIdAndTenantId(dto.getLeaveType().getId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        double requestedDays = calculateDays(dto.getStartDate(), dto.getEndDate(), dto.isHalfDay());

        String academicYear = "2024-2025";
        LeaveBalance balance = leaveBalanceRepository.findByTenantIdAndTargetUserIdAndLeaveTypeIdAndAcademicYear(
                tenantId, requesterId, leaveType.getId(), academicYear)
                .orElseThrow(() -> new RuntimeException("No leave balance found for this type."));

        if (balance.getBalance() < requestedDays) {
            throw new IllegalArgumentException("Insufficient leave balance. Available: " + balance.getBalance());
        }

        LeaveRequest request = new LeaveRequest();
        request.setTenantId(tenantId);
        request.setRequesterId(requesterId);
        request.setLeaveType(leaveType);
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setHalfDay(dto.isHalfDay());
        request.setReason(dto.getReason());
        request.setStatus(LeaveStatus.PENDING);

        request = leaveRequestRepository.save(request);
        return mapToDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getMyRequests() {
        String tenantId = getTenantId();
        Long requesterId = getCurrentUserId();
        return leaveRequestRepository.findByTenantIdAndRequesterId(tenantId, requesterId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getPendingRequests() {
        String tenantId = getTenantId();
        return leaveRequestRepository.findByTenantIdAndStatus(tenantId, LeaveStatus.PENDING).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveRequestDto processRequest(Long requestId, LeaveActionRequestDto action) {
        String tenantId = getTenantId();
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Request is already processed.");
        }

        request.setStatus(action.getStatus());
        request.setApproverComments(action.getComments());
        request.setActionedAt(LocalDateTime.now());

        if (action.getStatus() == LeaveStatus.APPROVED) {
            deductBalance(request);
            syncAttendanceOnApproval(request);
        }

        request = leaveRequestRepository.save(request);
        triggerStatusNotification(request);

        return mapToDto(request);
    }

    @Override
    @Transactional
    public void cancelRequest(Long requestId) {
        String tenantId = getTenantId();
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (request.getStatus() == LeaveStatus.APPROVED) {
            refundBalance(request);
        }

        request.setStatus(LeaveStatus.CANCELLED);
        leaveRequestRepository.save(request);
    }

    private void deductBalance(LeaveRequest request) {
        double days = calculateDays(request.getStartDate(), request.getEndDate(), request.isHalfDay());
        String academicYear = "2024-2025";
        LeaveBalance balance = leaveBalanceRepository.findByTenantIdAndTargetUserIdAndLeaveTypeIdAndAcademicYear(
                request.getTenantId(), request.getRequesterId(), request.getLeaveType().getId(), academicYear)
                .orElseThrow(() -> new RuntimeException("Balance record not found during deduction"));

        balance.setTotalUsed(balance.getTotalUsed() + days);
        balance.setBalance(balance.getTotalGranted() - balance.getTotalUsed());
        leaveBalanceRepository.save(balance);
    }

    private void refundBalance(LeaveRequest request) {
        double days = calculateDays(request.getStartDate(), request.getEndDate(), request.isHalfDay());
        String academicYear = "2024-2025";
        LeaveBalance balance = leaveBalanceRepository.findByTenantIdAndTargetUserIdAndLeaveTypeIdAndAcademicYear(
                request.getTenantId(), request.getRequesterId(), request.getLeaveType().getId(), academicYear)
                .orElseThrow(() -> new RuntimeException("Balance record not found during refund"));

        balance.setTotalUsed(balance.getTotalUsed() - days);
        balance.setBalance(balance.getTotalGranted() - balance.getTotalUsed());
        leaveBalanceRepository.save(balance);
    }

    private void triggerStatusNotification(LeaveRequest request) {
        try {
            Teacher teacher = teacherRepository.findById(request.getRequesterId()).orElse(null);
            if (teacher != null && teacher.getUser() != null) {
                notificationService.sendLeaveStatusNotification(
                        teacher.getUser().getId(),
                        request.getLeaveType().getName(),
                        request.getStatus().toString(),
                        request.getApproverComments());
            }
        } catch (Exception e) {
            log.error("Failed to send leave status notification: {}", e.getMessage());
        }
    }

    private void syncAttendanceOnApproval(LeaveRequest request) {
        LocalDate current = request.getStartDate();
        while (!current.isAfter(request.getEndDate())) {
            try {
                teacherAttendanceService.markAttendance(
                        request.getRequesterId(),
                        current,
                        AttendanceStatus.LEAVE,
                        "Leave Approved: " + request.getLeaveType().getName(),
                        "SYSTEM");
            } catch (Exception e) {
                log.warn("Failed to auto-mark attendance for date {}: {}", current, e.getMessage());
            }
            current = current.plusDays(1);
        }
    }

    private double calculateDays(LocalDate start, LocalDate end, boolean isHalfDay) {
        if (isHalfDay)
            return 0.5;
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    private LeaveRequestDto mapToDto(LeaveRequest entity) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(entity.getId());
        dto.setRequesterId(entity.getRequesterId());

        Teacher teacher = teacherRepository.findById(entity.getRequesterId()).orElse(null);
        if (teacher != null) {
            dto.setRequesterName(teacher.getName());
        }

        LeaveTypeDto typeDto = new LeaveTypeDto();
        typeDto.setId(entity.getLeaveType().getId());
        typeDto.setName(entity.getLeaveType().getName());
        dto.setLeaveType(typeDto);

        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setHalfDay(entity.isHalfDay());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        dto.setApproverId(entity.getApproverId());
        dto.setApproverComments(entity.getApproverComments());
        dto.setActionedAt(entity.getActionedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
