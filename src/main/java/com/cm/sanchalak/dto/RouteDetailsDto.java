package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for route details with stops and vehicle information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDetailsDto {
    
    private Long routeId;
    private String routeName;
    private String routeCode;
    private String routeType;
    private Integer estimatedDurationMinutes;
    private Double distanceKm;
    
    // Vehicle information
    private VehicleInfo vehicleInfo;
    
    // Student's stop information
    private StopInfo assignedStop;
    
    // All stops on the route
    private List<StopInfo> stops;
    
    // Trip information
    private TripInfo currentTrip;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        private Long vehicleId;
        private String vehicleNumber;
        private String vehicleType;
        private Integer capacity;
        private String makeModel;
        private String driverName;
        private String driverPhone;
        private boolean hasGpsTracking;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StopInfo {
        private Long stopId;
        private String stopName;
        private Integer stopOrder;
        private Double latitude;
        private Double longitude;
        private String landmark;
        private String scheduledTime; // HH:mm format
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripInfo {
        private Long tripId;
        private String tripDate;
        private String tripType;
        private String status;
        private String scheduledStartTime;
        private String actualStartTime;
        private String scheduledEndTime;
        private String actualEndTime;
    }
}
