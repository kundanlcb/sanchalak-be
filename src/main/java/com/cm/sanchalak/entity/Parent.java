package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Parent entity for student guardians
 * Links to User entity for authentication
 */
@Entity
@Table(name = "parents", indexes = {
        @Index(name = "idx_parent_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_parent_mobile", columnList = "mobile_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private java.util.UUID schoolId;

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

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "parent_id", length = 50, unique = true)
    private String parentID;

    public String getFullName() {
        return (firstName != null ? firstName : "") +
                (firstName != null && lastName != null ? " " : "") +
                (lastName != null ? lastName : "");
    }
}
