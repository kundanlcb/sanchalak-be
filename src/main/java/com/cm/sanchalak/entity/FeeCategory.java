package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "fee_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "school_id", "name" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;
}
