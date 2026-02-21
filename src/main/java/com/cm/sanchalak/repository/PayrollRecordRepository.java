package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {

    List<PayrollRecord> findAllByOrderByPaidAtDesc();

    List<PayrollRecord> findByMonth(String month);

    Optional<PayrollRecord> findByTeacherIdAndMonth(Long teacherId, String month);
}
