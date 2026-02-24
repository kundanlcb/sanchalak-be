package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId; // Links student to user account for authentication. Nullable for legacy data.

    @Column(name = "school_id")
    private UUID schoolId;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 50)
    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(name = "roll_no")
    private Integer rollNo;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String name; // Kept for backward compatibility, sync with first+last

    @Column(length = 10)
    private String gender;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_mobile")
    private String guardianMobile;

    @Column(name = "admission_number", length = 50)
    private String admissionNumber;

    @Column(name = "admission_date")
    private java.time.LocalDate admissionDate;

    @Column(length = 20)
    private String section;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_city")
    private String addressCity;

    @Column(name = "address_state")
    private String addressState;

    @Column(name = "address_pincode")
    private String addressPincode;

    @Column(name = "address_country")
    private String addressCountry;

    // Extended fields for admit card, marksheet, and admission form
    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "mother_name")
    private String motherName;

    @Column(name = "student_aadhar", length = 12)
    private String studentAadhar;

    @Column(name = "father_aadhar", length = 12)
    private String fatherAadhar;

    @Column(name = "mother_aadhar", length = 12)
    private String motherAadhar;

    @Column(name = "address_village")
    private String addressVillage;

    @Column(name = "address_district")
    private String addressDistrict;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "is_disabled")
    private Boolean isDisabled;

    @Column(name = "photo_url")
    private String photoUrl;

    private String parentRelationship;

    @Column(name = "parent_email")
    private String parentEmail;

    @Column(name = "parent_occupation")
    private String parentOccupation;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StudentStatus status = StudentStatus.ACTIVE;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass studentClass;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateName();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateName();
    }

    private void updateName() {
        this.name = (this.firstName != null ? this.firstName : "") +
                (this.firstName != null && this.lastName != null ? " " : "") +
                (this.lastName != null ? this.lastName : "");
        this.name = this.name.trim();
    }
}
