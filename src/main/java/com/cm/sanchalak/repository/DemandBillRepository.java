package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.DemandBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DemandBillRepository extends JpaRepository<DemandBill, Long> {

    List<DemandBill> findByStudentIdOrderByBillDateDesc(Long studentId);

    @Query("SELECT COUNT(d) FROM DemandBill d WHERE d.schoolId = :schoolId AND d.monthLabel = :monthLabel")
    int countBySchoolIdAndMonthLabel(@Param("schoolId") UUID schoolId, @Param("monthLabel") String monthLabel);

    boolean existsByStudentIdAndMonthLabel(Long studentId, String monthLabel);
}
