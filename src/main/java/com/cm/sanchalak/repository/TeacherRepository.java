package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    java.util.List<Teacher> findByDeletedFalse();
    long countByDeletedFalse();
}
