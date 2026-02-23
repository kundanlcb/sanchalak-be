package com.cm.sanchalak.repository.hr;

import com.cm.sanchalak.entity.hr.LeaveRequest;
import com.cm.sanchalak.entity.hr.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByTenantIdAndRequesterId(String tenantId, Long requesterId);

    List<LeaveRequest> findByTenantIdAndStatus(String tenantId, LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.tenantId = :tenantId AND lr.requesterId = :requesterId " +
            "AND lr.status != 'REJECTED' AND lr.status != 'CANCELLED' " +
            "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingRequests(
            @Param("tenantId") String tenantId,
            @Param("requesterId") Long requesterId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
