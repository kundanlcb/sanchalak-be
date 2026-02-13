package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.AttendanceRecord;
import com.cm.sanchalak.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByStudentIdAndDate(Long studentId, LocalDate date);
    
    boolean existsByStudentIdAndDate(Long studentId, LocalDate date);

    List<AttendanceRecord> findBySchoolClass_IdAndDate(Long classId, LocalDate date);

    List<AttendanceRecord> findByStudentId(Long studentId);

    List<AttendanceRecord> findByStudentIdAndDateBetween(Long studentId, LocalDate startDate, LocalDate endDate);
    
    // For history between dates (used in summary or reports)
    List<AttendanceRecord> findBySchoolClass_IdAndDateBetween(Long classId, LocalDate startDate, LocalDate endDate);

    long countByDateAndStatus(LocalDate date, AttendanceStatus status);
}
