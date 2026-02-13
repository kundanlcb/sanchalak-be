package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity representing a trip (daily route execution)
 */
@Entity
@Table(name = "trips", indexes = {
    @Index(name = "idx_trip_route_date", columnList = "route_id, trip_date"),
    @Index(name = "idx_trip_vehicle_date", columnList = "vehicle_id, trip_date"),
    @Index(name = "idx_trip_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class Trip extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    @Column(name = "trip_date", nullable = false)
    private LocalDate tripDate;
    
    @Column(name = "trip_type", nullable = false, length = 20)
    private String tripType; // MORNING_PICKUP, AFTERNOON_DROP
    
    @Column(name = "scheduled_start_time")
    private LocalTime scheduledStartTime;
    
    @Column(name = "actual_start_time")
    private LocalTime actualStartTime;
    
    @Column(name = "scheduled_end_time")
    private LocalTime scheduledEndTime;
    
    @Column(name = "actual_end_time")
    private LocalTime actualEndTime;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    
    @Column(name = "driver_name", length = 100)
    private String driverName;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
