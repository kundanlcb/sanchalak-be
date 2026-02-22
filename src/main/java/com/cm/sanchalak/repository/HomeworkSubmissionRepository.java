package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.HomeworkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository
        extends JpaRepository<HomeworkSubmission, Long>, JpaSpecificationExecutor<HomeworkSubmission> {

    Optional<HomeworkSubmission> findByHomeworkIdAndStudentId(Long homeworkId, Long studentId);

    boolean existsByHomeworkIdAndStudentId(Long homeworkId, Long studentId);

    List<HomeworkSubmission> findByHomeworkId(Long homeworkId);

    List<HomeworkSubmission> findByStudentId(Long studentId);
}
