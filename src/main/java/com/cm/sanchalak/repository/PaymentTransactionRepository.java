package com.cm.sanchalak.repository;

import com.cm.sanchalak.dto.analytics.CollectionTrendDto;
import com.cm.sanchalak.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository
                extends JpaRepository<PaymentTransaction, Long>, JpaSpecificationExecutor<PaymentTransaction> {
        List<PaymentTransaction> findByStudentIdAndSchoolIdOrderByPaymentDateDesc(Long studentId, UUID schoolId);

        List<PaymentTransaction> findByStudentId(Long studentId);

        boolean existsByTransactionReference(String reference);

        @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.schoolId = :schoolId AND pt.status = 'SUCCESS'")
        BigDecimal sumTotalCollected(@Param("schoolId") UUID schoolId);

        @Query("SELECT new com.cm.sanchalak.dto.analytics.CollectionTrendDto(CAST(pt.paymentDate AS LocalDate), SUM(pt.amount), COUNT(pt)) "
                        +
                        "FROM PaymentTransaction pt " +
                        "WHERE pt.schoolId = :schoolId AND pt.status = 'SUCCESS' " +
                        "AND pt.paymentDate BETWEEN :start AND :end " +
                        "GROUP BY CAST(pt.paymentDate AS LocalDate) " +
                        "ORDER BY CAST(pt.paymentDate AS LocalDate) ASC")
        List<CollectionTrendDto> findCollectionTrend(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("schoolId") UUID schoolId);
}
