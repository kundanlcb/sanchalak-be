package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Parent entity for student guardians
 * Links to User entity for authentication
 */
@Entity
@Table(name = "parents", indexes = {
        @Index(name = "idx_parent_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_parent_mobile", columnList = "mobile_number")
})
public class Parent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)", foreignKey = @ForeignKey(name = "fk_parent_user"))
    private User user;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Size(max = 15)
    @Column(name = "mobile_number", length = 15, unique = true)
    private String mobileNumber;

    @Email
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @Size(max = 255)
    @Column(length = 255)
    private String address;

    @Size(max = 50)
    @Column(length = 50)
    private String occupation;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "parent_id", length = 50, unique = true)
    private String parentID;

    public String getFullName() {
        return (firstName != null ? firstName : "") +
                (firstName != null && lastName != null ? " " : "") +
                (lastName != null ? lastName : "");
    }

    public Parent() {
    }

    public Parent(Long id, User user, String firstName, String lastName, String mobileNumber, String email,
            String address, String occupation, boolean isActive) {
        this.id = id;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.address = address;
        this.occupation = occupation;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }
}
