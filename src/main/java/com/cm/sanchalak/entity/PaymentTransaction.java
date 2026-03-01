package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "school_id")
    private UUID schoolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_fee_map_id")
    private StudentFeeMap studentFeeMap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_category_id")
    private FeeCategory feeCategory;

    @Column(name = "for_month")
    private String forMonth;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // CASH, ONLINE, CHEQUE

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, PENDING

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
}
