package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.LocationPing;
import com.cm.sanchalak.entity.Vehicle;
import com.cm.sanchalak.repository.LocationPingRepository;
import com.cm.sanchalak.repository.VehicleRepository;
import com.cm.sanchalak.repository.spec.LocationSpecification;
import com.cm.sanchalak.repository.spec.TransportSpecification;
import com.cm.sanchalak.repository.spec.VehicleSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Service for GPS location tracking
 * Handles location ping ingestion and staleness detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationTrackingService {

    private final LocationPingRepository locationPingRepository;
    private final VehicleRepository vehicleRepository;
    private final OwnershipValidator ownership;

    // GPS data is considered stale after 2 minutes
    private static final Duration STALENESS_THRESHOLD = Duration.ofMinutes(2);

    /**
     * Record a location ping from a GPS device
     */
    @Transactional
    public LocationPing recordLocationPing(LocationPing ping) {
        // Assuming the device recording the ping is authorized for the vehicle
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
    @Transactional(readOnly = true)
    public LocationPing getLatestLocationForVehicle(Long vehicleId) {
        vehicleRepository.findOne(VehicleSpecification.activeById(vehicleId))
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Instant twoMinutesAgo = Instant.now().minus(STALENESS_THRESHOLD);

        return locationPingRepository.findAll(LocationSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("vehicle").get("id"), vehicleId))
                .and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("receivedAt"), twoMinutesAgo)),
                org.springframework.data.domain.PageRequest.of(0, 1,
                        org.springframework.data.domain.Sort.by("receivedAt").descending()))
                .getContent().stream().findFirst().orElse(null);
    }

    /**
     * Get the latest location ping for a vehicle without time limit
     * Returns the most recent ping even if stale
     */
    @Transactional(readOnly = true)
    public LocationPing getLatestLocationForVehicleNoTimeLimit(Long vehicleId) {
        vehicleRepository.findOne(VehicleSpecification.activeById(vehicleId))
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        return locationPingRepository.findAll(LocationSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("vehicle").get("id"), vehicleId)),
                org.springframework.data.domain.PageRequest.of(0, 1,
                        org.springframework.data.domain.Sort.by("receivedAt").descending()))
                .getContent().stream().findFirst().orElse(null);
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
    @Transactional(readOnly = true)
    public List<LocationPing> getLocationHistoryForTrip(Long tripId) {
        return locationPingRepository.findAll(LocationSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("trip").get("id"), tripId)),
                org.springframework.data.domain.Sort.by("receivedAt").ascending());
    }

    /**
     * Get location history for a vehicle within a time range
     */
    @Transactional(readOnly = true)
    public List<LocationPing> getLocationHistoryForVehicle(Long vehicleId, Instant startTime, Instant endTime) {
        vehicleRepository.findOne(VehicleSpecification.activeById(vehicleId))
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        return locationPingRepository.findAll(LocationSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("vehicle").get("id"), vehicleId))
                .and((root, query, cb) -> cb.between(root.get("receivedAt"), startTime, endTime)),
                org.springframework.data.domain.Sort.by("receivedAt").ascending());
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
