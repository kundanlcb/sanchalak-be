package com.cm.sanchalak.platform.school;

import com.cm.sanchalak.entity.RoleName;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "school_role_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolRolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID schoolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleName roleName;

    @Column(nullable = false)
    private String featureCode;
}
