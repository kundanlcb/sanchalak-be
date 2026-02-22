package com.cm.sanchalak.platform.location;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "states", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "code", "country_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String code; // e.g. "BR" for Bihar

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
