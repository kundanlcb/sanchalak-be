package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    boolean existsByNameAndAcademicYear(String name, String academicYear);
    Optional<FeeStructure> findByNameAndAcademicYear(String name, String academicYear);
}
