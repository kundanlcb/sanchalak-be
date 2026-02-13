package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.exception.ResourceNotFoundException;
import com.cm.sanchalak.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for transport management operations
 */
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
    
    /**
     * Get route details by ID
     */
    public Route getRouteById(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));
    }
    
    /**
     * Get all active stops for a route, ordered by stop order
     */
    public List<Stop> getStopsByRouteId(Long routeId) {
        return stopRepository.findByRouteIdOrderByStopOrder(routeId);
    }
    
    /**
     * Get active transport assignment for a student
     * Cached for 6 hours to reduce database load
     */
    @Cacheable(value = "route-assignments", key = "#studentId")
    public StudentTransportAssignment getActiveAssignmentForStudent(Long studentId) {
        LocalDate today = LocalDate.now();
        return assignmentRepository.findActiveByStudentId(studentId, today)
            .orElse(null); // Return null if student doesn't use transport
    }
    
    /**
     * Check if student has an active transport assignment
     */
    public boolean hasActiveTransportAssignment(Long studentId) {
        return getActiveAssignmentForStudent(studentId) != null;
    }
    
    /**
     * Get all students assigned to a route
     */
    public List<StudentTransportAssignment> getStudentsByRouteId(Long routeId) {
        return assignmentRepository.findActiveByRouteId(routeId);
    }
    
    /**
     * Get all students assigned to a stop
     */
    public List<StudentTransportAssignment> getStudentsByStopId(Long stopId) {
        return assignmentRepository.findActiveByStopId(stopId);
    }
    
    /**
     * Get vehicle by ID
     */
    public Vehicle getVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
    }
    
    /**
     * Get vehicle by GPS device ID
     */
    public Vehicle getVehicleByGpsDeviceId(String gpsDeviceId) {
        return vehicleRepository.findByGpsDeviceId(gpsDeviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with GPS device ID: " + gpsDeviceId));
    }
    
    /**
     * Get active trip for a route on a specific date
     */
    public Trip getActiveTripForRoute(Long routeId, LocalDate tripDate) {
        return tripRepository.findActiveByRouteIdAndTripDate(routeId, tripDate)
            .orElse(null); // Return null if no active trip
    }
    
    /**
     * Get all trips for a vehicle on a specific date
     */
    public List<Trip> getTripsByVehicleAndDate(Long vehicleId, LocalDate tripDate) {
        return tripRepository.findByVehicleIdAndTripDate(vehicleId, tripDate);
    }
    
    /**
     * Get stop by ID
     */
    public Stop getStopById(Long stopId) {
        return stopRepository.findById(stopId)
            .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));
    }
    
    /**
     * Count active students on a route
     */
    public long countStudentsOnRoute(Long routeId) {
        return assignmentRepository.countActiveByRouteId(routeId);
    }
}
