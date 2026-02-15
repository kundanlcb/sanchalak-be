package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "subjects")
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

    public Subject() {
    }

    public Subject(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
