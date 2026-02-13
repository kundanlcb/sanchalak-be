package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Entity for GPS location pings from vehicles
 * capturedAt: timestamp when GPS device captured the location
 * receivedAt: timestamp when server received the ping
 */
@Entity
@Table(name = "location_pings", indexes = {
    @Index(name = "idx_ping_vehicle_received", columnList = "vehicle_id, received_at DESC"),
    @Index(name = "idx_ping_trip", columnList = "trip_id"),
    @Index(name = "idx_ping_received", columnList = "received_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class LocationPing extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;
    
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    @Column(name = "speed_kmh")
    private Double speedKmh;
    
    @Column(name = "heading")
    private Double heading; // Direction in degrees (0-360)
    
    @Column(name = "accuracy_meters")
    private Double accuracyMeters;
    
    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt; // When GPS device captured this location
    
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt; // When server received this ping
    
    @Column(name = "device_id", length = 50)
    private String deviceId;
}
