package com.cm.sanchalak.platform.master;

import com.cm.sanchalak.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "master_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "domain_id", nullable = false)
    private MasterDomain domain;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String label;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // Additional metadata can be added here as needed (e.g., JSON column)
}
