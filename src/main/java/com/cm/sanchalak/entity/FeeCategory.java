package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fee_categories")
@Getter
@Setter
@NoArgsConstructor
public class FeeCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    public FeeCategory(String name, String description, Boolean isMandatory) {
        this.name = name;
        this.description = description;
        this.isMandatory = isMandatory != null ? isMandatory : false;
    }
}
