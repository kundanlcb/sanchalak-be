package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.exception.ResourceNotFoundException;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.repository.spec.TransportSpecification;
import com.cm.sanchalak.repository.spec.StudentSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransportService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final StudentTransportAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final StudentRepository studentRepository;
    private final OwnershipValidator ownership;

    public Route getRouteById(Long routeId) {
        return routeRepository.findOne(TransportSpecification.routeById(routeId))
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));
    }

    public List<Stop> getStopsByRouteId(Long routeId) {
        routeRepository.findOne(TransportSpecification.routeById(routeId))
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));

        return stopRepository.findAll(TransportSpecification.stopScoped()
                .and((root, query, cb) -> cb.equal(root.get("route").get("id"), routeId)))
                .stream()
                .sorted((s1, s2) -> s1.getStopOrder().compareTo(s2.getStopOrder()))
                .toList();
    }

    @Cacheable(value = "route-assignments", key = "#studentId")
    public StudentTransportAssignment getActiveAssignmentForStudent(Long studentId) {
        if (studentId != null) {
            studentRepository.findOne(StudentSpecification.activeById(studentId))
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        }

        LocalDate today = LocalDate.now();
        return assignmentRepository.findAll(TransportSpecification.assignmentScoped()
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                .and((root, query, cb) -> cb.isTrue(root.get("isActive")))
                .and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), today))
                .and((root, query, cb) -> cb.or(cb.isNull(root.get("endDate")),
                        cb.greaterThanOrEqualTo(root.get("endDate"), today))))
                .stream().findFirst().orElse(null);
    }

    public boolean hasActiveTransportAssignment(Long studentId) {
        return getActiveAssignmentForStudent(studentId) != null;
    }

    public List<StudentTransportAssignment> getStudentsByRouteId(Long routeId) {
        routeRepository.findOne(TransportSpecification.routeById(routeId))
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));

        return assignmentRepository.findAll(TransportSpecification.assignmentScoped()
                .and((root, query, cb) -> cb.equal(root.get("route").get("id"), routeId))
                .and((root, query, cb) -> cb.isTrue(root.get("isActive"))));
    }

    public List<StudentTransportAssignment> getStudentsByStopId(Long stopId) {
        stopRepository.findOne(TransportSpecification.stopById(stopId))
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found"));

        return assignmentRepository.findAll(TransportSpecification.assignmentScoped()
                .and((root, query, cb) -> cb.or(
                        cb.equal(root.get("pickupStop").get("id"), stopId),
                        cb.equal(root.get("dropStop").get("id"), stopId)))
                .and((root, query, cb) -> cb.isTrue(root.get("isActive"))));
    }

    public Vehicle getVehicleById(Long vehicleId) {
        return vehicleRepository.findOne(TransportSpecification.vehicleById(vehicleId))
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
    }

    public Vehicle getVehicleByGpsDeviceId(String gpsDeviceId) {
        return vehicleRepository.findOne(TransportSpecification.vehicleScoped()
                .and((root, query, cb) -> cb.equal(root.get("gpsDeviceId"), gpsDeviceId)))
                .orElseThrow(
                        () -> new ResourceNotFoundException("Vehicle not found with GPS device ID: " + gpsDeviceId));
    }

    public Trip getActiveTripForRoute(Long routeId, LocalDate tripDate) {
        routeRepository.findOne(TransportSpecification.routeById(routeId))
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));

        return tripRepository.findAll(TransportSpecification.tripScoped()
                .and((root, query, cb) -> cb.equal(root.get("route").get("id"), routeId))
                .and((root, query, cb) -> cb.equal(root.get("tripDate"), tripDate))
                .and((root, query, cb) -> root.get("status").in("SCHEDULED", "ACTIVE")))
                .stream().findFirst().orElse(null);
    }

    public List<Trip> getTripsByVehicleAndDate(Long vehicleId, LocalDate tripDate) {
        vehicleRepository.findOne(TransportSpecification.vehicleById(vehicleId))
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        return tripRepository.findAll(TransportSpecification.tripScoped()
                .and((root, query, cb) -> cb.equal(root.get("vehicle").get("id"), vehicleId))
                .and((root, query, cb) -> cb.equal(root.get("tripDate"), tripDate)));
    }

    public Stop getStopById(Long stopId) {
        return stopRepository.findOne(TransportSpecification.stopById(stopId))
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));
    }

    public long countStudentsOnRoute(Long routeId) {
        routeRepository.findOne(TransportSpecification.routeById(routeId))
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        return assignmentRepository.count(TransportSpecification.assignmentScoped()
                .and((root, query, cb) -> cb.equal(root.get("route").get("id"), routeId))
                .and((root, query, cb) -> cb.isTrue(root.get("isActive"))));
    }
}
