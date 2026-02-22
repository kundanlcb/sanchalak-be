package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.StudentTransportAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentTransportAssignmentRepository extends JpaRepository<StudentTransportAssignment, Long>,
              JpaSpecificationExecutor<StudentTransportAssignment> {

       @Query("SELECT sta FROM StudentTransportAssignment sta WHERE sta.student.id = :studentId " +
                     "AND :today BETWEEN sta.effectiveFrom AND (CASE WHEN sta.effectiveTo IS NULL THEN '2999-12-31' ELSE sta.effectiveTo END) "
                     +
                     "AND sta.isActive = true")
       Optional<StudentTransportAssignment> findActiveByStudentId(@Param("studentId") Long studentId,
                     @Param("today") LocalDate today);

       @Query("SELECT sta FROM StudentTransportAssignment sta WHERE sta.route.id = :routeId AND sta.isActive = true")
       List<StudentTransportAssignment> findActiveByRouteId(@Param("routeId") Long routeId);

       @Query("SELECT sta FROM StudentTransportAssignment sta WHERE sta.stop.id = :stopId AND sta.isActive = true")
       List<StudentTransportAssignment> findActiveByStopId(@Param("stopId") Long stopId);

       @Query("SELECT COUNT(sta) FROM StudentTransportAssignment sta WHERE sta.route.id = :routeId AND sta.isActive = true")
       long countActiveByRouteId(@Param("routeId") Long routeId);
}
