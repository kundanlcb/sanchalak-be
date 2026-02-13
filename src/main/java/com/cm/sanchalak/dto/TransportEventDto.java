package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transport events (pickup, drop, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportEventDto {
    
    private Long eventId;
    private Long tripId;
    private Long studentId;
    private String studentName;
    private Long stopId;
    private String stopName;
    
    // Event details
    private String eventType; // PICKED_UP, DROPPED_OFF, ABSENT, CANCELLED
    private String eventTimestamp; // ISO-8601 format
    private String eventTime; // HH:mm format
    
    // Location information
    private Double latitude;
    private Double longitude;
    
    // Metadata
    private String recordedBy; // Driver name or system
    private String remarks;
    
    // Trip context
    private String tripDate;
    private String tripType;
    private String routeName;
    private String vehicleNumber;
}
