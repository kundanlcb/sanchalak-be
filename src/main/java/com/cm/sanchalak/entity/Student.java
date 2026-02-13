package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "students")
public class Student extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 10)
    private String gender;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_mobile")
    private String guardianMobile;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Class studentClass;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianMobile() {
        return guardianMobile;
    }

    public void setGuardianMobile(String guardianMobile) {
        this.guardianMobile = guardianMobile;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Class getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(Class studentClass) {
        this.studentClass = studentClass;
    }
}
