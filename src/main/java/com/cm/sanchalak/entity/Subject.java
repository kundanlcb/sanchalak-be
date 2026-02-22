package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "subjects", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "code", "school_id", "class_id" })
})
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

    @Column(name = "class_id")
    private Long classId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;
}
