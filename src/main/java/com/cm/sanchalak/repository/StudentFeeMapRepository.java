package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.StudentFeeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentFeeMapRepository extends JpaRepository<StudentFeeMap, Long> {
    List<StudentFeeMap> findByStudentId(Long studentId);
    boolean existsByStudentIdAndFeeStructureId(Long studentId, Long structureId);
    boolean existsByFeeStructureId(Long structureId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(item.amount) FROM StudentFeeMap sfm JOIN sfm.feeStructure fs JOIN fs.items item")
    java.math.BigDecimal sumTotalBaseFee();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(sfm.discountAmount) FROM StudentFeeMap sfm")
    java.math.BigDecimal sumTotalDiscounts();
}
