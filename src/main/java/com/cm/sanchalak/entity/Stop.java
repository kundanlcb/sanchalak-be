package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Entity representing a stop on a route
 */
@Entity
@Table(name = "stops", indexes = {
        @Index(name = "idx_stop_route", columnList = "route_id, stop_order"),
        @Index(name = "idx_stop_location", columnList = "latitude, longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Stop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "stop_name", nullable = false, length = 200)
    private String stopName;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "scheduled_arrival_time")
    private LocalTime scheduledArrivalTime;

    @Column(name = "landmark", length = 200)
    private String landmark;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
