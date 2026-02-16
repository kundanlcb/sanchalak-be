package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a transport vehicle (bus/van)
 */
@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_number", columnList = "vehicle_number", unique = true),
        @Index(name = "idx_vehicle_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 20)
    private String vehicleNumber;

    @Column(name = "vehicle_type", length = 20)
    private String vehicleType; // BUS, VAN, MINI_BUS

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "make_model", length = 100)
    private String makeModel;

    @Column(name = "registration_year")
    private Integer registrationYear;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    @Column(name = "gps_device_id", length = 50)
    private String gpsDeviceId;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
