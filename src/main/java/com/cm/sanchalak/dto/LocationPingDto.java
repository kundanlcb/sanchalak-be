package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for location ping ingestion (from GPS device)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationPingDto {
    
    // Device authentication
    private String deviceId;
    private String gpsDeviceId;
    
    // Location data
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double heading; // Direction in degrees (0-360)
    private Double accuracyMeters;
    
    // Timestamp when GPS captured this location
    private String capturedAt; // ISO-8601 format
    
    // Optional: Trip context
    private Long tripId;
}
