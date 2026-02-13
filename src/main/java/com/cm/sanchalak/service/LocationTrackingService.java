package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.LocationPing;
import com.cm.sanchalak.entity.Vehicle;
import com.cm.sanchalak.repository.LocationPingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for GPS location tracking
 * Handles location ping ingestion and staleness detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationTrackingService {
    
    private final LocationPingRepository locationPingRepository;
    
    // GPS data is considered stale after 2 minutes
    private static final Duration STALENESS_THRESHOLD = Duration.ofMinutes(2);
    
    /**
     * Record a location ping from a GPS device
     */
    @Transactional
    public LocationPing recordLocationPing(LocationPing ping) {
        ping.setReceivedAt(Instant.now());
        LocationPing saved = locationPingRepository.save(ping);
        log.debug("Recorded location ping for vehicle {} at ({}, {})", 
            ping.getVehicle().getId(), ping.getLatitude(), ping.getLongitude());
        return saved;
    }
    
    /**
     * Get the latest location ping for a vehicle
     * Returns null if no ping found or data is stale (older than 2 minutes)
     */
    public LocationPing getLatestLocationForVehicle(Long vehicleId) {
        Instant twoMinutesAgo = Instant.now().minus(STALENESS_THRESHOLD);
        return locationPingRepository.findLatestByVehicleId(vehicleId, twoMinutesAgo)
            .orElse(null);
    }
    
    /**
     * Get the latest location ping for a vehicle without time limit
     * Returns the most recent ping even if stale
     */
    public LocationPing getLatestLocationForVehicleNoTimeLimit(Long vehicleId) {
        return locationPingRepository.findLatestByVehicleIdNoTimeLimit(vehicleId)
            .orElse(null);
    }
    
    /**
     * Check if GPS data for a vehicle is stale
     * Data is stale if the last ping was received more than 2 minutes ago
     */
    public boolean isLocationDataStale(Long vehicleId) {
        LocationPing latest = getLatestLocationForVehicle(vehicleId);
        return latest == null;
    }
    
    /**
     * Get staleness status with details
     */
    public StalenessStatus getStalenessStatus(Long vehicleId) {
        LocationPing latest = getLatestLocationForVehicleNoTimeLimit(vehicleId);
        
        if (latest == null) {
            return new StalenessStatus(true, null, "No GPS data available");
        }
        
        Instant now = Instant.now();
        Duration timeSinceLastPing = Duration.between(latest.getReceivedAt(), now);
        boolean isStale = timeSinceLastPing.compareTo(STALENESS_THRESHOLD) > 0;
        
        String message = isStale 
            ? String.format("GPS data is stale (last update %d seconds ago)", timeSinceLastPing.getSeconds())
            : "GPS data is current";
        
        return new StalenessStatus(isStale, latest, message);
    }
    
    /**
     * Get all location pings for a trip
     */
    public List<LocationPing> getLocationHistoryForTrip(Long tripId) {
        return locationPingRepository.findByTripId(tripId);
    }
    
    /**
     * Get location history for a vehicle within a time range
     */
    public List<LocationPing> getLocationHistoryForVehicle(Long vehicleId, Instant startTime, Instant endTime) {
        return locationPingRepository.findByVehicleIdAndTimeRange(vehicleId, startTime, endTime);
    }
    
    /**
     * Class to hold staleness status information
     */
    public static class StalenessStatus {
        private final boolean isStale;
        private final LocationPing latestPing;
        private final String message;
        
        public StalenessStatus(boolean isStale, LocationPing latestPing, String message) {
            this.isStale = isStale;
            this.latestPing = latestPing;
            this.message = message;
        }
        
        public boolean isStale() {
            return isStale;
        }
        
        public LocationPing getLatestPing() {
            return latestPing;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
