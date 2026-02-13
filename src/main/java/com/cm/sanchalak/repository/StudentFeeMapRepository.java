package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.StudentFeeMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentFeeMapRepository extends JpaRepository<StudentFeeMap, Long> {
    List<StudentFeeMap> findByStudentId(Long studentId);
    boolean existsByStudentIdAndFeeStructureId(Long studentId, Long structureId);
}
