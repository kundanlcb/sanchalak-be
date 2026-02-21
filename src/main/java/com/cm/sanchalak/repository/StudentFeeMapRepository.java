package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.StudentFeeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentFeeMapRepository extends JpaRepository<StudentFeeMap, Long> {
    List<StudentFeeMap> findByStudentId(Long studentId);
    List<StudentFeeMap> findByStudentIdAndIsActiveTrue(Long studentId);
    List<StudentFeeMap> findByStudentIdAndSchoolIdAndIsActiveTrue(Long studentId, UUID schoolId);
    Optional<StudentFeeMap> findTopByStudentIdAndIsActiveTrueOrderByCreatedAtAsc(Long studentId);
    boolean existsByStudentIdAndFeeStructureId(Long studentId, Long structureId);
    boolean existsByStudentIdAndClassFeeAssignmentId(Long studentId, Long assignmentId);
    boolean existsByFeeStructureId(Long structureId);
    boolean existsByFeeStructureIdAndSchoolId(Long structureId, UUID schoolId);

    @Query("SELECT SUM(item.amount) FROM StudentFeeMap sfm JOIN sfm.feeStructure fs JOIN fs.items item WHERE sfm.isActive = true")
    BigDecimal sumTotalBaseFee();

    @Query("SELECT SUM(sfm.discountAmount) FROM StudentFeeMap sfm WHERE sfm.isActive = true")
    BigDecimal sumTotalDiscounts();

    @Query("SELECT SUM(item.amount) FROM StudentFeeMap sfm JOIN sfm.feeStructure fs JOIN fs.items item WHERE sfm.isActive = true AND sfm.schoolId = :schoolId")
    BigDecimal sumTotalBaseFeeBySchoolId(UUID schoolId);

    @Query("SELECT SUM(sfm.discountAmount) FROM StudentFeeMap sfm WHERE sfm.isActive = true AND sfm.schoolId = :schoolId")
    BigDecimal sumTotalDiscountsBySchoolId(UUID schoolId);
}
