package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByStudentId(Long studentId);
    boolean existsByTransactionReference(String transactionReference);
}
