package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Adding schoolId for multi-tenancy
    @Column(name = "school_id")
    private UUID schoolId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;
}
