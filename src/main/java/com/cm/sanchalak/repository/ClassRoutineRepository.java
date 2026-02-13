package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ClassRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRoutineRepository extends JpaRepository<ClassRoutine, Long> {
    List<ClassRoutine> findByStudentClassId(Long classId);
    
    // Check conflicts
    boolean existsByStudentClassIdAndDayOfWeekAndPeriod(Long classId, DayOfWeek dayOfWeek, Integer period);
    boolean existsByTeacherIdAndDayOfWeekAndPeriod(Long teacherId, DayOfWeek dayOfWeek, Integer period);
}
