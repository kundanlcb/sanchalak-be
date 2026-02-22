package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    List<Vehicle> findBySchoolId(UUID schoolId);

    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    Optional<Vehicle> findByGpsDeviceId(String gpsDeviceId);
}
