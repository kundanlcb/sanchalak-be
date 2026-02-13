package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByStudentClass_Id(Long classId);
    long countByStudentClassId(Long classId);
    long countByGender(String gender);
}
