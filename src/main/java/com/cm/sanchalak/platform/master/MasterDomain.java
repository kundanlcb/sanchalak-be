package com.cm.sanchalak.platform.master;

import com.cm.sanchalak.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "master_domains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterDomain extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean isSystem = false;

    @Column(name = "is_school_scoped", nullable = false)
    @Builder.Default
    private boolean isSchoolScoped = false;
}
