package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.academics.Holiday;
import com.cm.sanchalak.entity.academics.HolidayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

        List<Holiday> findByTenantIdAndAcademicYear(String tenantId, String academicYear);

        List<Holiday> findByTenantIdIsNullAndAcademicYear(String academicYear);

        // Get all holidays applicable to a tenant (Tenant specific + Generic National)
        @Query("SELECT h FROM Holiday h WHERE (h.tenantId = :tenantId OR h.tenantId IS NULL) AND h.academicYear = :academicYear")
        List<Holiday> findAllActiveHolidaysForTenant(@Param("tenantId") String tenantId,
                        @Param("academicYear") String academicYear);

        // Query to check if a specific date overlaps with any holiday
        @Query("SELECT h FROM Holiday h WHERE (h.tenantId = :tenantId OR h.tenantId IS NULL) " +
                        "AND (:targetDate BETWEEN h.startDate AND h.endDate) " +
                        "AND h.applicableToStaff = true")
        List<Holiday> findOverlappingStaffHolidays(@Param("targetDate") LocalDate targetDate,
                        @Param("tenantId") String tenantId);

        @Query("SELECT h FROM Holiday h WHERE (h.tenantId = :tenantId OR h.tenantId IS NULL) " +
                        "AND (:targetDate BETWEEN h.startDate AND h.endDate) " +
                        "AND h.applicableToStudents = true")
        List<Holiday> findOverlappingStudentHolidays(@Param("targetDate") LocalDate targetDate,
                        @Param("tenantId") String tenantId);

        long countByTypeAndAcademicYear(HolidayType type, String academicYear);
}
