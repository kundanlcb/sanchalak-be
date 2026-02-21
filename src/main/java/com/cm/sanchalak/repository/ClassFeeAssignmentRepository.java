package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ClassFeeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassFeeAssignmentRepository extends JpaRepository<ClassFeeAssignment, Long> {

    Optional<ClassFeeAssignment> findBySchoolIdAndStudentClassIdAndFeeStructureIdAndAcademicYearAndEffectiveFrom(
            UUID schoolId,
            Long classId,
            Long structureId,
            String academicYear,
            LocalDate effectiveFrom);
}
