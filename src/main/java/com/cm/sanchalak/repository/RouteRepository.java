package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    
    Optional<Route> findByRouteCode(String routeCode);
    
    @Query("SELECT r FROM Route r WHERE r.isActive = true ORDER BY r.routeName")
    List<Route> findAllActive();
    
    @Query("SELECT r FROM Route r WHERE r.isActive = true AND r.routeType = :routeType")
    List<Route> findByRouteTypeAndActive(String routeType);
    
    @Query("SELECT r FROM Route r WHERE r.vehicle.id = :vehicleId AND r.isActive = true")
    List<Route> findByVehicleIdAndActive(Long vehicleId);
}
