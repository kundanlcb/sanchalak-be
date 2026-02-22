package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ClassRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface ClassRoutineRepository
        extends JpaRepository<ClassRoutine, Long>, JpaSpecificationExecutor<ClassRoutine> {
    List<ClassRoutine> findByStudentClassId(Long classId);

    List<ClassRoutine> findByTeacherId(Long teacherId);

    // Check conflicts
    boolean existsByStudentClassIdAndDayOfWeekAndPeriod(Long classId, DayOfWeek dayOfWeek, Integer period);

    boolean existsByTeacherIdAndDayOfWeekAndPeriod(Long teacherId, DayOfWeek dayOfWeek, Integer period);

    // Integrity checks
    boolean existsByTeacherId(Long teacherId);

    boolean existsBySubjectId(Long subjectId);
}
