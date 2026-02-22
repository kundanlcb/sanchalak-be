package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {

    @Query("SELECT t FROM Trip t WHERE t.route.id = :routeId AND t.tripDate = :tripDate AND t.status IN ('SCHEDULED', 'ACTIVE')")
    Optional<Trip> findActiveByRouteIdAndTripDate(@Param("routeId") Long routeId,
            @Param("tripDate") LocalDate tripDate);

    List<Trip> findByVehicleIdAndTripDate(Long vehicleId, LocalDate tripDate);
}
