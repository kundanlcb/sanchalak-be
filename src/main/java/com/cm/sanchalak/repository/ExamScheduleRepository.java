package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamScheduleRepository
        extends JpaRepository<ExamSchedule, Long>, JpaSpecificationExecutor<ExamSchedule> {

    /**
     * Find exam schedules for a class within a date range
     */
    List<ExamSchedule> findByStudentClassIdAndExamDateBetween(Long classId, LocalDate startDate, LocalDate endDate);

    /**
     * Find specific exam schedule by term, class, and subject
     */
    java.util.Optional<ExamSchedule> findByExamTerm_IdAndStudentClass_IdAndSubject_Id(Long termId, Long classId,
            Long subjectId);

    /**
     * Find exam schedules by term and class
     */
    List<ExamSchedule> findByExamTerm_IdAndStudentClass_Id(Long termId, Long classId);
}
