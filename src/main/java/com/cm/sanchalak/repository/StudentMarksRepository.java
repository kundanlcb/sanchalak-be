package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ExamSchedule;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.StudentMarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentMarksRepository extends JpaRepository<StudentMarks, Long> {
    Optional<StudentMarks> findByExamScheduleAndStudent(ExamSchedule examSchedule, Student student);
    java.util.List<StudentMarks> findByStudent(Student student);
    
    // For Analytics
    java.util.List<StudentMarks> findByStudent_IdAndExamSchedule_ExamTerm_Id(Long studentId, Long termId);
}
