package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    @Query("SELECT t FROM Trip t WHERE t.route.id = :routeId AND t.tripDate = :tripDate AND t.status IN ('SCHEDULED', 'IN_PROGRESS')")
    Optional<Trip> findActiveByRouteIdAndTripDate(Long routeId, LocalDate tripDate);
    
    @Query("SELECT t FROM Trip t WHERE t.vehicle.id = :vehicleId AND t.tripDate = :tripDate ORDER BY t.scheduledStartTime")
    List<Trip> findByVehicleIdAndTripDate(Long vehicleId, LocalDate tripDate);
    
    @Query("SELECT t FROM Trip t WHERE t.route.id = :routeId AND t.tripDate BETWEEN :startDate AND :endDate ORDER BY t.tripDate DESC, t.scheduledStartTime DESC")
    List<Trip> findByRouteIdAndDateRange(Long routeId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Trip t WHERE t.tripDate = :tripDate AND t.status = :status")
    List<Trip> findByDateAndStatus(LocalDate tripDate, String status);
}
