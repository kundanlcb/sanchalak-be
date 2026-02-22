package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Page<Student> findByDeletedFalse(Pageable pageable);

    Page<Student> findBySchoolIdAndDeletedFalse(UUID schoolId, Pageable pageable);

    List<Student> findByDeletedFalse();

    List<Student> findBySchoolIdAndDeletedFalse(UUID schoolId);

    List<Student> findByDeletedTrue();

    // Original methods should ideally filter by deleted=false.
    // For now we will explicitly use these for non-deleted lookups.
    List<Student> findByStudentClass_IdAndDeletedFalse(Long classId);

    // Keep legacy for existing code that might not care, or update them.
    List<Student> findByStudentClass_Id(Long classId);

    List<Student> findByStudentClass_SchoolIdAndDeletedFalse(UUID schoolId);

    // Find student by user account
    Optional<Student> findByUserId(UUID userId);

    long countByStudentClassId(Long classId);

    long countByGender(String gender);

    long countByDeletedFalse();

    long countBySchoolIdAndDeletedFalse(UUID schoolId);

}
