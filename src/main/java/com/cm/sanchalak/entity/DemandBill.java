package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "demand_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandBill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "bill_no", nullable = false)
    private String billNo; // Auto-generated: e.g., "MDN-2025-06-001"

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @Column(name = "month_label", length = 30)
    private String monthLabel; // e.g., "JUNE 2025"

    @Column(name = "total_current_fees", nullable = false)
    private BigDecimal totalCurrentFees;

    @Column(name = "total_back_dues", nullable = false)
    private BigDecimal totalBackDues;

    @Column(name = "grand_total", nullable = false)
    private BigDecimal grandTotal;

    @Builder.Default
    @OneToMany(mappedBy = "demandBill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DemandBillLineItem> lineItems = new ArrayList<>();
}
