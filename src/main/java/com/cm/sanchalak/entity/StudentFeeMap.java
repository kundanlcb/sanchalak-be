package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "student_fee_maps", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "student_id", "class_fee_assignment_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFeeMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id", nullable = false)
    private FeeStructure feeStructure;

    @Column(name = "school_id")
    private UUID schoolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_fee_assignment_id")
    private ClassFeeAssignment classFeeAssignment;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "discount_reason")
    private String discountReason;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
