package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ExamSchedule;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.StudentMarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface StudentMarksRepository
                extends JpaRepository<StudentMarks, Long>, JpaSpecificationExecutor<StudentMarks> {
        Optional<StudentMarks> findByExamScheduleAndStudent(ExamSchedule examSchedule, Student student);

        List<StudentMarks> findByStudent(Student student);

        List<StudentMarks> findByExamSchedule_StudentClass_IdAndExamSchedule_Subject_IdAndExamSchedule_ExamTerm_Id(
                        Long classId, Long subjectId, Long termId);

        // For Analytics
        List<StudentMarks> findByStudent_IdAndExamSchedule_ExamTerm_Id(Long studentId, Long termId);

        @Query(value = "SELECT t.name as teacherName, AVG(sm.marks_obtained) as avgMarks " +
                        "FROM student_marks sm " +
                        "JOIN exam_schedules es ON sm.exam_schedule_id = es.id " +
                        "JOIN class_subjects cs ON es.class_id = cs.class_id AND es.subject_id = cs.subject_id " +
                        "JOIN teachers t ON cs.teacher_id = t.id " +
                        "GROUP BY t.id, t.name", nativeQuery = true)
        List<Map<String, Object>> findTeacherPerformance();
}
