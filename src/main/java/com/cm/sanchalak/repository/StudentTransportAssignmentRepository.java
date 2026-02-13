package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.StudentTransportAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentTransportAssignmentRepository extends JpaRepository<StudentTransportAssignment, Long> {
    
    @Query("SELECT sta FROM StudentTransportAssignment sta " +
           "WHERE sta.student.id = :studentId " +
           "AND sta.isActive = true " +
           "AND sta.effectiveFrom <= :currentDate " +
           "AND (sta.effectiveTo IS NULL OR sta.effectiveTo >= :currentDate)")
    Optional<StudentTransportAssignment> findActiveByStudentId(Long studentId, LocalDate currentDate);
    
    @Query("SELECT sta FROM StudentTransportAssignment sta " +
           "WHERE sta.route.id = :routeId AND sta.isActive = true")
    List<StudentTransportAssignment> findActiveByRouteId(Long routeId);
    
    @Query("SELECT sta FROM StudentTransportAssignment sta " +
           "WHERE sta.stop.id = :stopId AND sta.isActive = true")
    List<StudentTransportAssignment> findActiveByStopId(Long stopId);
    
    @Query("SELECT COUNT(sta) FROM StudentTransportAssignment sta " +
           "WHERE sta.route.id = :routeId AND sta.isActive = true")
    long countActiveByRouteId(Long routeId);
}
