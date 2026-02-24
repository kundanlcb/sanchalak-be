package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "demand_bill_line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandBillLineItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_bill_id", nullable = false)
    private DemandBill demandBill;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "months_upto", length = 30)
    private String monthsUpto; // e.g., "JUNE", "April", or "Apr - May (Back due)"

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "is_back_due")
    @Builder.Default
    private Boolean isBackDue = false;
}
