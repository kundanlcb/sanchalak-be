package com.cm.sanchalak.entity.hr;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "leave_balances", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tenant_id", "target_user_id", "leave_type_id", "academic_year" })
})
@Getter
@Setter
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(nullable = false)
    private double totalGranted;

    @Column(nullable = false)
    private double totalUsed;

    @Column(nullable = false)
    private double balance;

    @Version
    private Long version;
}
