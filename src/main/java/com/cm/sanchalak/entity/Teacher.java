package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @NotBlank
    @Email
    @Column(length = 50, unique = true)
    private String email;

    @NotBlank
    @Size(max = 15)
    @Column(name = "phone", length = 15)
    private String mobileNumber;

    @Size(max = 100)
    @Column(length = 100)
    private String qualification;

    @Column(name = "profile_image")
    private String profileImage;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "teacher_specializations", joinColumns = @JoinColumn(name = "teacher_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private Set<Subject> specializations = new HashSet<>();

    @Column(name = "teacher_id", length = 50, unique = true)
    private String teacherID;

    @Column(name = "joining_date")
    private String joiningDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    public String getPhone() {
        return mobileNumber;
    }

    public void setPhone(String phone) {
        this.mobileNumber = phone;
    }
}
