package com.cm.sanchalak.repository.hr;

import com.cm.sanchalak.entity.hr.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findByTenantId(String tenantId);

    Optional<LeaveType> findByIdAndTenantId(Long id, String tenantId);
}
