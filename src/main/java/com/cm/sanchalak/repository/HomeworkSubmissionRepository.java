package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.HomeworkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {
    
    /**
     * Find submission by homework and student
     */
    Optional<HomeworkSubmission> findByHomeworkIdAndStudentId(Long homeworkId, Long studentId);
    
    /**
     * Check if student has submitted homework
     */
    boolean existsByHomeworkIdAndStudentId(Long homeworkId, Long studentId);
    
    /**
     * Find all submissions for a homework
     */
    List<HomeworkSubmission> findByHomeworkId(Long homeworkId);
    
    /**
     * Find all submissions by a student
     */
    List<HomeworkSubmission> findByStudentId(Long studentId);
}
