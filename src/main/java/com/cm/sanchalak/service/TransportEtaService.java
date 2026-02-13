package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.LocationPing;
import com.cm.sanchalak.entity.Stop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/**
 * Service for calculating estimated time of arrival (ETA) for buses
 * Uses Haversine formula to calculate distance between GPS coordinates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransportEtaService {
    
    private final LocationTrackingService locationTrackingService;
    
    // Average bus speed assumptions (km/h)
    private static final double DEFAULT_SPEED_KMH = 30.0; // Urban traffic
    private static final double MIN_SPEED_KMH = 10.0;     // Heavy traffic
    private static final double MAX_SPEED_KMH = 50.0;     // Open road
    
    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Calculate ETA to a stop based on current vehicle location and speed
     * Returns estimated minutes until arrival, or null if GPS data is unavailable/stale
     */
    public Integer calculateEtaMinutes(Long vehicleId, Stop stop) {
        LocationPing currentLocation = locationTrackingService.getLatestLocationForVehicle(vehicleId);
        
        if (currentLocation == null) {
            log.debug("No current location available for vehicle {}, cannot calculate ETA", vehicleId);
            return null;
        }
        
        // Calculate distance using Haversine formula
        double distanceKm = calculateHaversineDistance(
            currentLocation.getLatitude(), 
            currentLocation.getLongitude(),
            stop.getLatitude(), 
            stop.getLongitude()
        );
        
        // Determine speed to use for ETA calculation
        double speedKmh = determineSpeed(currentLocation);
        
        // Calculate ETA in minutes
        double etaMinutes = (distanceKm / speedKmh) * 60.0;
        
        log.debug("ETA calculation for vehicle {} to stop {}: distance={}km, speed={}km/h, eta={}min",
            vehicleId, stop.getId(), String.format("%.2f", distanceKm), 
            String.format("%.1f", speedKmh), Math.round(etaMinutes));
        
        return (int) Math.round(etaMinutes);
    }
    
    /**
     * Calculate ETA for multiple stops on a route
     * Accumulates travel time between consecutive stops
     */
    public void calculateEtasForStops(Long vehicleId, List<Stop> stops, List<Integer> etas) {
        LocationPing currentLocation = locationTrackingService.getLatestLocationForVehicle(vehicleId);
        
        if (currentLocation == null) {
            // No GPS data, all ETAs are null
            for (int i = 0; i < stops.size(); i++) {
                etas.add(null);
            }
            return;
        }
        
        double currentLat = currentLocation.getLatitude();
        double currentLon = currentLocation.getLongitude();
        double speedKmh = determineSpeed(currentLocation);
        double cumulativeMinutes = 0.0;
        
        for (Stop stop : stops) {
            double distanceKm = calculateHaversineDistance(currentLat, currentLon, stop.getLatitude(), stop.getLongitude());
            double segmentMinutes = (distanceKm / speedKmh) * 60.0;
            cumulativeMinutes += segmentMinutes;
            
            etas.add((int) Math.round(cumulativeMinutes));
            
            // Update current position for next segment
            currentLat = stop.getLatitude();
            currentLon = stop.getLongitude();
        }
    }
    
    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * Returns distance in kilometers
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Determine speed to use for ETA calculation
     * Prefers current GPS speed, falls back to default assumptions
     */
    private double determineSpeed(LocationPing locationPing) {
        if (locationPing.getSpeedKmh() != null && locationPing.getSpeedKmh() > 0) {
            // Use GPS-reported speed, but clamp to reasonable bounds
            double speed = locationPing.getSpeedKmh();
            if (speed < MIN_SPEED_KMH) {
                return MIN_SPEED_KMH;
            } else if (speed > MAX_SPEED_KMH) {
                return MAX_SPEED_KMH;
            }
            return speed;
        }
        
        // No speed data, use default
        return DEFAULT_SPEED_KMH;
    }
    
    /**
     * Calculate time difference from scheduled time
     * Returns minutes ahead (negative) or behind (positive) schedule
     */
    public Integer calculateScheduleDeviation(LocalTime scheduledTime, Integer etaMinutes) {
        if (scheduledTime == null || etaMinutes == null) {
            return null;
        }
        
        LocalTime now = LocalTime.now();
        LocalTime estimatedArrival = now.plusMinutes(etaMinutes);
        
        Duration deviation = Duration.between(scheduledTime, estimatedArrival);
        return (int) deviation.toMinutes();
    }
}
