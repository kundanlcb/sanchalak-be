package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for stops with ETA information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopEtaDto {
    
    private Long stopId;
    private String stopName;
    private Integer stopOrder;
    private Double latitude;
    private Double longitude;
    private String landmark;
    
    // Scheduled timing
    private String scheduledArrivalTime; // HH:mm format
    
    // Real-time ETA information
    private Integer estimatedArrivalMinutes; // Minutes from now, null if GPS unavailable
    private String estimatedArrivalTime;     // Calculated time, null if GPS unavailable
    private Integer scheduleDeviationMinutes; // Negative = ahead, Positive = behind, null if no schedule
    private String etaStatus; // ON_TIME, EARLY, DELAYED, UNKNOWN
    
    // Student count at this stop
    private Integer studentsAssigned;
    
    // Distance from current position
    private Double distanceKm; // null if GPS unavailable
    
    /**
     * List of stops with ETA information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StopsWithEtaResponse {
        private Long routeId;
        private String routeName;
        private Long vehicleId;
        private String vehicleNumber;
        private boolean hasLiveTracking;
        private String lastUpdateTime;
        private List<StopEtaDto> stops;
    }
}
