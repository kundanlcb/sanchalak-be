package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fee_structures", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "school_id", "name", "academic_year" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false)
    private String name;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(nullable = false)
    private String frequency; // MONTHLY, ANNUAL, QUARTERLY

    @Column(name = "late_fee_amount")
    private BigDecimal lateFeeAmount;

    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;

    @Builder.Default
    @OneToMany(mappedBy = "feeStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeeStructureItem> items = new ArrayList<>();
}
