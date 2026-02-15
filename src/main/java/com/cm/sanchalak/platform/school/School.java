package com.cm.sanchalak.platform.school;

import com.cm.sanchalak.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "schools", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "school_code"
        }),
        @UniqueConstraint(columnNames = {
                "registration_number"
        })
})
public class School extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "school_code", length = 20, nullable = false)
    private String schoolCode;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String name;

    @Size(max = 50)
    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private SchoolStatus status = SchoolStatus.DRAFT;

    @Column(length = 50)
    private String timezone;

    @Column(length = 50)
    private String board;

    @Embedded
    private ContactInfo contactInfo;

    public School() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public SchoolStatus getStatus() {
        return status;
    }

    public void setStatus(SchoolStatus status) {
        this.status = status;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }
}
