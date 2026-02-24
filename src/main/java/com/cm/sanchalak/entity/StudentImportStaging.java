package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "student_import_staging")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentImportStaging extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private UUID schoolId;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String admissionNo;
    private String className;
    private String parentName;
    private String parentPhone;

    @Builder.Default
    private boolean processed = false;

    private String errorMessage;
}
