package com.cm.sanchalak.platform.location;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 3)
    private String code; // ISO 3166-1 alpha-2 (IN, US, etc.)

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
