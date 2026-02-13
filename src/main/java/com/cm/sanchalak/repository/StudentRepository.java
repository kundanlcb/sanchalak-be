package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByDeletedFalse();
    List<Student> findByDeletedTrue();
    
    // Original methods should ideally filter by deleted=false. 
    // For now we will explicitly use these for non-deleted lookups.
    List<Student> findByStudentClass_IdAndDeletedFalse(Long classId);
    
    // Keep legacy for existing code that might not care, or update them.
    List<Student> findByStudentClass_Id(Long classId);
    
    long countByStudentClassId(Long classId);
    long countByGender(String gender);
    long countByDeletedFalse();
}
