package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.StudentFeeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentFeeMapRepository
        extends JpaRepository<StudentFeeMap, Long>, JpaSpecificationExecutor<StudentFeeMap> {
    List<StudentFeeMap> findByStudentIdAndSchoolIdAndIsActiveTrue(Long studentId, UUID schoolId);

    List<StudentFeeMap> findByStudentIdAndIsActiveTrue(Long studentId);

    boolean existsByStudentIdAndClassFeeAssignmentId(Long studentId, Long assignmentId);

    Optional<StudentFeeMap> findTopByStudentIdAndIsActiveTrueOrderByCreatedAtAsc(Long studentId);

    boolean existsByFeeStructureIdAndSchoolId(Long feeStructureId, UUID schoolId);

    @Query("SELECT SUM(fsi.amount) FROM StudentFeeMap sfm JOIN sfm.feeStructure fs JOIN fs.items fsi WHERE sfm.schoolId = :schoolId AND sfm.isActive = true")
    BigDecimal sumTotalBaseFee(@Param("schoolId") UUID schoolId);

    @Query("SELECT SUM(sfm.discountAmount) FROM StudentFeeMap sfm WHERE sfm.schoolId = :schoolId AND sfm.isActive = true")
    BigDecimal sumTotalDiscounts(@Param("schoolId") UUID schoolId);
}
