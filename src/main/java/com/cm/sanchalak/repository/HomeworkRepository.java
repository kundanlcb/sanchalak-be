package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long>, JpaSpecificationExecutor<Homework> {

        List<Homework> findByStudentClassId(Long classId);

        @Query("SELECT h FROM Homework h WHERE " +
                        "(:classId IS NULL OR h.studentClass.id = :classId) AND " +
                        "(:subjectId IS NULL OR h.subject.id = :subjectId) AND " +
                        "(:dueDate IS NULL OR h.dueDate = :dueDate)")
        List<Homework> findWithFilters(@Param("classId") Long classId,
                        @Param("subjectId") Long subjectId,
                        @Param("dueDate") LocalDate dueDate);
}
