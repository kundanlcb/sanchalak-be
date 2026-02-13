package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for live GPS location of a vehicle
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveLocationDto {
    
    private Long vehicleId;
    private String vehicleNumber;
    private Long routeId;
    private String routeName;
    
    // GPS coordinates
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double heading; // Direction in degrees (0-360)
    
    // Timing information
    private String capturedAt; // ISO-8601 timestamp
    private String receivedAt; // ISO-8601 timestamp
    private Integer secondsSinceLastUpdate;
    
    // Data quality indicators
    private boolean isStale; // True if last update > 2 minutes ago
    private String stalenessMessage;
    private Double accuracyMeters;
    
    // Trip context
    private Long currentTripId;
    private String tripStatus;
}
