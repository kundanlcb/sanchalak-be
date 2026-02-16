package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a transport route
 */
@Entity
@Table(name = "routes", indexes = {
        @Index(name = "idx_route_name", columnList = "route_name"),
        @Index(name = "idx_route_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Route extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_name", nullable = false, length = 100)
    private String routeName;

    @Column(name = "route_code", length = 20)
    private String routeCode;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "route_type", length = 20)
    private String routeType; // MORNING_PICKUP, AFTERNOON_DROP, BOTH

    @Column(name = "start_location", length = 200)
    private String startLocation;

    @Column(name = "end_location", length = 200)
    private String endLocation;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
