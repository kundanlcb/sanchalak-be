package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    
    /**
     * Find all homework for a specific class
     */
    List<Homework> findByStudentClassId(Long classId);
}
