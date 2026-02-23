package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.TeacherAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherAttendanceRepository
        extends JpaRepository<TeacherAttendance, Long>, JpaSpecificationExecutor<TeacherAttendance> {
    Optional<TeacherAttendance> findByTeacherIdAndDate(Long teacherId, LocalDate date);

    List<TeacherAttendance> findBySchoolIdAndDate(UUID schoolId, LocalDate date);

    List<TeacherAttendance> findByTeacherIdAndDateBetweenOrderByDateDesc(Long teacherId, LocalDate startDate,
            LocalDate endDate);

    List<TeacherAttendance> findBySchoolIdAndDateBetween(UUID schoolId, LocalDate startDate, LocalDate endDate);
}
