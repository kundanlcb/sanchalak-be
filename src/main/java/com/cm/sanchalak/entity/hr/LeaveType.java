package com.cm.sanchalak.entity.hr;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Multi-tenant isolation
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isPaid;

    @Column(nullable = false)
    private int defaultAnnualQuota;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "leave_type_roles", joinColumns = @JoinColumn(name = "leave_type_id"))
    @Column(name = "role_name")
    private List<String> applicableRoles;

    @Column(nullable = false)
    private boolean requiresDocumentUpload;
}
