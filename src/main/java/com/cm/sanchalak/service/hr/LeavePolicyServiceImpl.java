package com.cm.sanchalak.service.hr;

import com.cm.sanchalak.dto.hr.LeaveBalanceDto;
import com.cm.sanchalak.dto.hr.LeaveTypeDto;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.entity.hr.LeaveBalance;
import com.cm.sanchalak.entity.hr.LeaveType;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.hr.LeaveBalanceRepository;
import com.cm.sanchalak.repository.hr.LeaveTypeRepository;
import com.cm.sanchalak.security.SchoolContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeavePolicyServiceImpl implements LeavePolicyService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final TeacherRepository teacherRepository;

    private String getTenantId() {
        UUID schoolId = SchoolContext.getSchoolId();
        if (schoolId == null) {
            throw new RuntimeException("No active school context found for tenant operations.");
        }
        return schoolId.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeDto> getAllLeaveTypes() {
        String tenantId = getTenantId();
        return leaveTypeRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveTypeDto createLeaveType(LeaveTypeDto dto) {
        String tenantId = getTenantId();
        LeaveType leaveType = new LeaveType();
        leaveType.setTenantId(tenantId);
        updateEntityFromDto(leaveType, dto);

        leaveType = leaveTypeRepository.save(leaveType);
        return mapToDto(leaveType);
    }

    @Override
    @Transactional
    public LeaveTypeDto updateLeaveType(Long id, LeaveTypeDto dto) {
        String tenantId = getTenantId();
        LeaveType leaveType = leaveTypeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("LeaveType not found"));

        updateEntityFromDto(leaveType, dto);

        leaveType = leaveTypeRepository.save(leaveType);
        return mapToDto(leaveType);
    }

    @Override
    @Transactional
    public void deleteLeaveType(Long id) {
        String tenantId = getTenantId();
        LeaveType leaveType = leaveTypeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("LeaveType not found"));
        leaveTypeRepository.delete(leaveType);
    }

    @Override
    @Transactional
    public void initializeTeacherBalances(String academicYear) {
        String tenantId = getTenantId();
        log.info("Initializing teacher leave balances for tenant {} and academic year {}", tenantId, academicYear);

        List<LeaveType> leaveTypes = leaveTypeRepository.findByTenantId(tenantId);
        if (leaveTypes.isEmpty()) {
            log.warn("No leave types defined for tenant. Skipping initialization.");
            return;
        }

        UUID schoolId = UUID.fromString(tenantId);
        List<Teacher> activeTeachers = teacherRepository.findBySchoolIdAndDeletedFalse(schoolId);

        for (Teacher teacher : activeTeachers) {
            for (LeaveType leaveType : leaveTypes) {
                // Initialize balance if it doesn't exist
                Optional<LeaveBalance> existingBalance = leaveBalanceRepository
                        .findByTenantIdAndTargetUserIdAndLeaveTypeIdAndAcademicYear(
                                tenantId, teacher.getId(), leaveType.getId(), academicYear);

                if (existingBalance.isEmpty()) {
                    LeaveBalance balance = new LeaveBalance();
                    balance.setTenantId(tenantId);
                    balance.setTargetUserId(teacher.getId());
                    balance.setLeaveType(leaveType);
                    balance.setAcademicYear(academicYear);
                    balance.setTotalGranted(leaveType.getDefaultAnnualQuota());
                    balance.setTotalUsed(0);
                    balance.setBalance(leaveType.getDefaultAnnualQuota()); // Initial balance

                    leaveBalanceRepository.save(balance);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getLeaveBalances(Long targetUserId, String academicYear) {
        String tenantId = getTenantId();
        return leaveBalanceRepository.findByTenantIdAndTargetUserIdAndAcademicYear(tenantId, targetUserId, academicYear)
                .stream()
                .map(this::mapToBalanceDto)
                .collect(Collectors.toList());
    }

    private void updateEntityFromDto(LeaveType entity, LeaveTypeDto dto) {
        entity.setName(dto.getName());
        entity.setPaid(dto.isPaid());
        entity.setDefaultAnnualQuota(dto.getDefaultAnnualQuota());
        entity.setApplicableRoles(dto.getApplicableRoles());
        entity.setRequiresDocumentUpload(dto.isRequiresDocumentUpload());
    }

    private LeaveTypeDto mapToDto(LeaveType entity) {
        LeaveTypeDto dto = new LeaveTypeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPaid(entity.isPaid());
        dto.setDefaultAnnualQuota(entity.getDefaultAnnualQuota());
        dto.setApplicableRoles(entity.getApplicableRoles());
        dto.setRequiresDocumentUpload(entity.isRequiresDocumentUpload());
        return dto;
    }

    private LeaveBalanceDto mapToBalanceDto(LeaveBalance entity) {
        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setId(entity.getId());
        dto.setTargetUserId(entity.getTargetUserId());
        dto.setLeaveType(mapToDto(entity.getLeaveType()));
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setTotalGranted(entity.getTotalGranted());
        dto.setTotalUsed(entity.getTotalUsed());
        dto.setBalance(entity.getBalance());
        return dto;
    }
}
