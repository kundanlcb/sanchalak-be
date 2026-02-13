package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
    
    Optional<Vehicle> findByGpsDeviceId(String gpsDeviceId);
    
    @Query("SELECT v FROM Vehicle v WHERE v.isActive = true ORDER BY v.vehicleNumber")
    List<Vehicle> findAllActive();
    
    @Query("SELECT v FROM Vehicle v WHERE v.isActive = true AND v.gpsDeviceId IS NOT NULL")
    List<Vehicle> findAllActiveWithGps();
}
