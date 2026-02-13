package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, Long> {
    
    @Query("SELECT s FROM Stop s WHERE s.route.id = :routeId AND s.isActive = true ORDER BY s.stopOrder")
    List<Stop> findByRouteIdOrderByStopOrder(Long routeId);
    
    @Query("SELECT s FROM Stop s WHERE s.route.id = :routeId AND s.isActive = true")
    List<Stop> findActiveByRouteId(Long routeId);
    
    @Query("SELECT COUNT(s) FROM Stop s WHERE s.route.id = :routeId AND s.isActive = true")
    long countActiveByRouteId(Long routeId);
}
