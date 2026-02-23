package com.cm.sanchalak.repository.hr;

import com.cm.sanchalak.entity.hr.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByTenantIdAndTargetUserIdAndAcademicYear(String tenantId, Long targetUserId,
            String academicYear);

    Optional<LeaveBalance> findByTenantIdAndTargetUserIdAndLeaveTypeIdAndAcademicYear(String tenantId,
            Long targetUserId, Long leaveTypeId, String academicYear);
}
