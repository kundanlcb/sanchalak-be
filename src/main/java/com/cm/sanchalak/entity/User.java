package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                                "email"
                }),
                @UniqueConstraint(columnNames = {
                                "mobile_number"
                })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @NotBlank
        @Size(max = 40)
        @Column(length = 40, nullable = false)
        private String name;

        @NotBlank
        @Size(max = 40)
        @Email
        @Column(length = 40, nullable = false)
        private String email;

        @Size(max = 15)
        @Column(name = "mobile_number", length = 15, unique = true)
        private String mobileNumber; // For OTP authentication

        @NotBlank
        @Size(max = 100)
        @Column(length = 100, nullable = false)
        private String password;

        @Column(name = "school_id")
        private UUID schoolId;

        @Builder.Default
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
        private Set<Role> roles = new HashSet<>();
}
