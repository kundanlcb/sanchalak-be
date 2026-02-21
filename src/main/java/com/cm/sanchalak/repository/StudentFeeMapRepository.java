package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.StudentFeeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StudentFeeMapRepository extends JpaRepository<StudentFeeMap, Long> {
    List<StudentFeeMap> findByStudentId(Long studentId);
    boolean existsByStudentIdAndFeeStructureId(Long studentId, Long structureId);
    boolean existsByFeeStructureId(Long structureId);

    @Query("SELECT SUM(item.amount) FROM StudentFeeMap sfm JOIN sfm.feeStructure fs JOIN fs.items item")
    BigDecimal sumTotalBaseFee();

    @Query("SELECT SUM(sfm.discountAmount) FROM StudentFeeMap sfm")
    BigDecimal sumTotalDiscounts();
}
