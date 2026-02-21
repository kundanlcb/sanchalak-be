package com.cm.sanchalak.repository;

import com.cm.sanchalak.dto.analytics.CollectionTrendDto;
import com.cm.sanchalak.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByStudentId(Long studentId);
    boolean existsByTransactionReference(String transactionReference);

    @Query("SELECT SUM(p.amount) FROM PaymentTransaction p WHERE p.status = 'SUCCESS'")
    BigDecimal sumTotalCollected();

    @Query("SELECT new com.cm.sanchalak.dto.analytics.CollectionTrendDto(cast(p.paymentDate as LocalDate), SUM(p.amount), COUNT(p)) " +
            "FROM PaymentTransaction p WHERE p.status = 'SUCCESS' AND p.paymentDate BETWEEN :start AND :end " +
            "GROUP BY cast(p.paymentDate as LocalDate) ORDER BY cast(p.paymentDate as LocalDate)")
    List<CollectionTrendDto> findCollectionTrend(LocalDateTime start, LocalDateTime end);
}
