package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.LocationPing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationPingRepository extends JpaRepository<LocationPing, Long> {
    
    /**
     * Get the latest location ping for a vehicle within the last 2 minutes
     * Used to determine if GPS data is stale
     */
    @Query("SELECT lp FROM LocationPing lp " +
           "WHERE lp.vehicle.id = :vehicleId " +
           "AND lp.receivedAt >= :twoMinutesAgo " +
           "ORDER BY lp.receivedAt DESC LIMIT 1")
    Optional<LocationPing> findLatestByVehicleId(Long vehicleId, Instant twoMinutesAgo);
    
    @Query("SELECT lp FROM LocationPing lp " +
           "WHERE lp.vehicle.id = :vehicleId " +
           "ORDER BY lp.receivedAt DESC LIMIT 1")
    Optional<LocationPing> findLatestByVehicleIdNoTimeLimit(Long vehicleId);
    
    @Query("SELECT lp FROM LocationPing lp " +
           "WHERE lp.trip.id = :tripId " +
           "ORDER BY lp.receivedAt DESC")
    List<LocationPing> findByTripId(Long tripId);
    
    @Query("SELECT lp FROM LocationPing lp " +
           "WHERE lp.vehicle.id = :vehicleId " +
           "AND lp.receivedAt BETWEEN :startTime AND :endTime " +
           "ORDER BY lp.receivedAt")
    List<LocationPing> findByVehicleIdAndTimeRange(Long vehicleId, Instant startTime, Instant endTime);
}
