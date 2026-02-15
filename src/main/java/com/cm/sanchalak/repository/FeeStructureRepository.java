package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    List<FeeStructure> findBySchoolId(UUID schoolId);

    boolean existsByNameAndAcademicYearAndSchoolId(String name, String academicYear, UUID schoolId);

    boolean existsByNameAndAcademicYear(String name, String academicYear); // Legacy safety
}
