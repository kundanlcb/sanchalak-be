package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ExamQuestionRepository
        extends JpaRepository<ExamQuestion, Long>, JpaSpecificationExecutor<ExamQuestion> {
    List<ExamQuestion> findByExamSchedule_IdOrderBySequenceOrderAsc(Long examScheduleId);

    void deleteByExamSchedule_Id(Long examScheduleId);
}
