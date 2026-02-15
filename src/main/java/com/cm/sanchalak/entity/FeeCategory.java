package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "fee_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "school_id", "name" })
})
@Getter
@Setter
@NoArgsConstructor
public class FeeCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    public FeeCategory(UUID schoolId, String name, String description, Boolean isMandatory) {
        this.schoolId = schoolId;
        this.name = name;
        this.description = description;
        this.isMandatory = isMandatory != null ? isMandatory : false;
    }
}
