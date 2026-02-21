package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    List<ExamQuestion> findByExamSchedule_IdOrderBySequenceOrderAsc(Long examScheduleId);

    void deleteByExamSchedule_Id(Long examScheduleId);
}
