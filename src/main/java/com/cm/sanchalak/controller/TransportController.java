package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.*;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.exception.AuthorizationException;
import com.cm.sanchalak.exception.BusinessException;
import com.cm.sanchalak.exception.ResourceNotFoundException;
import com.cm.sanchalak.security.CurrentUser;
import com.cm.sanchalak.security.UserPrincipal;
import com.cm.sanchalak.service.*;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for transport/bus tracking endpoints
 * Unified API for both web and mobile clients
 */
@RestController
@RequestMapping("/api/transport")
@Tag(name = "Transport & Tracking", description = "Bus route, live location, limits and stops endpoints")
@RequiredArgsConstructor
@Slf4j
public class TransportController {
    
    private final TransportService transportService;
    private final LocationTrackingService locationTrackingService;
    private final TransportEtaService transportEtaService;
    private final TransportEventService transportEventService;
    private final ParentAuthorizationService parentAuthService;
    private final ParentService parentService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    
    /**
     * Get student's assigned transport route with details
     * STUDENT: Auto-resolves from JWT
     * PARENT: Requires ?studentId={id} with linkage validation
     */
    @Operation(summary = "Get Assigned Route", description = "Returns the transport route, vehicle, and stop assigned to the student. Parents must provide studentId.")
    @ApiResponse(responseCode = "200", description = "Route details returned")
    @ApiResponse(responseCode = "403", description = "Forbidden (Linkage validation failed)", content = @Content)
    @ApiResponse(responseCode = "404", description = "No active transport assignment", content = @Content)
    @GetMapping("/my-route")
    public ApiResult<RouteDetailsDto> getMyRoute(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "ID of the student (Required for PARENT role)") @RequestParam(required = false) Long studentId) {
        
        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        
        if (resolvedStudentId == null) {
            throw new BusinessException("STUDENT_RESOLUTION_FAILED", "Unable to resolve student");
        }
        
        // Get student's active transport assignment
        StudentTransportAssignment assignment = transportService.getActiveAssignmentForStudent(resolvedStudentId);
        
        if (assignment == null) {
            throw new ResourceNotFoundException("No active transport assignment found for this student");
        }
        
        Route route = assignment.getRoute();
        Vehicle vehicle = route.getVehicle();
        Stop assignedStop = assignment.getStop();
        
        // Get all stops on the route
        List<Stop> stops = transportService.getStopsByRouteId(route.getId());
        
        // Get current trip (today's trip)
        Trip currentTrip = transportService.getActiveTripForRoute(route.getId(), LocalDate.now());
        
        // Build response DTO
        RouteDetailsDto dto = RouteDetailsDto.builder()
            .routeId(route.getId())
            .routeName(route.getRouteName())
            .routeCode(route.getRouteCode())
            .routeType(route.getRouteType())
            .estimatedDurationMinutes(route.getEstimatedDurationMinutes())
            .distanceKm(route.getDistanceKm())
            .vehicleInfo(buildVehicleInfo(vehicle))
            .assignedStop(buildStopInfo(assignedStop))
            .stops(stops.stream().map(this::buildStopInfo).collect(Collectors.toList()))
            .currentTrip(currentTrip != null ? buildTripInfo(currentTrip) : null)
            .build();
        
        return ApiResult.success(dto);
    }
    
    /**
     * Get live GPS location of a vehicle on a route
     * Public for students/parents to track their assigned buses
     */
    @Operation(summary = "Get Live Bus Location", description = "Returns the latest GPS ping for the vehicle on the specified route.")
    @ApiResponse(responseCode = "200", description = "Live location returned")
    @ApiResponse(responseCode = "403", description = "User/Student not assigned to this route", content = @Content)
    @ApiResponse(responseCode = "404", description = "Vehicle/Route/Ping not found", content = @Content)
    @GetMapping("/live")
    public ApiResult<LiveLocationDto> getLiveLocation(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "Route ID to track") @RequestParam Long routeId,
            @Parameter(description = "ID of the student (Required for PARENT role)") @RequestParam(required = false) Long studentId) {
        
        // Authorization check: verify student has access to this route
        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        
        if (resolvedStudentId == null) {
            throw new BusinessException("STUDENT_RESOLUTION_FAILED", "Unable to resolve student");
        }
        
        StudentTransportAssignment assignment = transportService.getActiveAssignmentForStudent(resolvedStudentId);
        
        if (assignment == null || !assignment.getRoute().getId().equals(routeId)) {
            throw new AuthorizationException("AUTHZ_001", "You don't have access to this route");
        }
        
        Route route = transportService.getRouteById(routeId);
        Vehicle vehicle = route.getVehicle();
        
        if (vehicle == null) {
            throw new ResourceNotFoundException("No vehicle assigned to this route");
        }
        
        // Get staleness status with latest location
        LocationTrackingService.StalenessStatus stalenessStatus = 
            locationTrackingService.getStalenessStatus(vehicle.getId());
        
        LocationPing latestPing = stalenessStatus.getLatestPing();
        
        if (latestPing == null) {
            throw new ResourceNotFoundException("No GPS data available for this vehicle");
        }
        
        // Calculate time since last update
        Instant now = Instant.now();
        long secondsSinceUpdate = Duration.between(latestPing.getReceivedAt(), now).getSeconds();
        
        // Get current trip
        Trip currentTrip = transportService.getActiveTripForRoute(routeId, LocalDate.now());
        
        LiveLocationDto dto = LiveLocationDto.builder()
            .vehicleId(vehicle.getId())
            .vehicleNumber(vehicle.getVehicleNumber())
            .routeId(route.getId())
            .routeName(route.getRouteName())
            .latitude(latestPing.getLatitude())
            .longitude(latestPing.getLongitude())
            .speedKmh(latestPing.getSpeedKmh())
            .heading(latestPing.getHeading())
            .capturedAt(latestPing.getCapturedAt().toString())
            .receivedAt(latestPing.getReceivedAt().toString())
            .secondsSinceLastUpdate((int) secondsSinceUpdate)
            .isStale(stalenessStatus.isStale())
            .stalenessMessage(stalenessStatus.getMessage())
            .accuracyMeters(latestPing.getAccuracyMeters())
            .currentTripId(currentTrip != null ? currentTrip.getId() : null)
            .tripStatus(currentTrip != null ? currentTrip.getStatus() : null)
            .build();
        
        return ApiResult.success(dto);
    }
    
    /**
     * Get stops with ETA information for a route
     * Shows real-time ETA if GPS tracking is available
     */
    @Operation(summary = "Get Stops with ETA", description = "Returns ordered list of stops with estimated arrival times based on current bus location.")
    @ApiResponse(responseCode = "200", description = "Stops with ETA returned")
    @GetMapping("/stops")
    public ApiResult<StopEtaDto.StopsWithEtaResponse> getStopsWithEta(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "Route ID to track") @RequestParam Long routeId,
            @Parameter(description = "ID of the student (Required for PARENT role)") @RequestParam(required = false) Long studentId) {
        
        // Authorization check
        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        
        if (resolvedStudentId == null) {
            throw new BusinessException("STUDENT_RESOLUTION_FAILED", "Unable to resolve student");
        }
        
        StudentTransportAssignment assignment = transportService.getActiveAssignmentForStudent(resolvedStudentId);
        
        if (assignment == null || !assignment.getRoute().getId().equals(routeId)) {
            throw new AuthorizationException("AUTHZ_001", "You don't have access to this route");
        }
        
        Route route = transportService.getRouteById(routeId);
        Vehicle vehicle = route.getVehicle();
        List<Stop> stops = transportService.getStopsByRouteId(routeId);
        
        // Check if live tracking is available
        boolean hasLiveTracking = vehicle != null && vehicle.getGpsDeviceId() != null;
        LocationPing latestLocation = (hasLiveTracking && vehicle != null) ? 
            locationTrackingService.getLatestLocationForVehicle(vehicle.getId()) : null;
        
        // Build stop DTOs with ETA
        List<StopEtaDto> stopDtos = new ArrayList<>();
        
        for (Stop stop : stops) {
            Integer etaMinutes = null;
            Double distanceKm = null;
            String etaStatus = "UNKNOWN";
            Integer scheduleDeviation = null;
            String estimatedArrivalTime = null;
            
            if (latestLocation != null && vehicle != null) {
                // Calculate ETA
                etaMinutes = transportEtaService.calculateEtaMinutes(vehicle.getId(), stop);
                
                if (etaMinutes != null) {
                    // Calculate estimated arrival time
                    LocalTime eta = LocalTime.now().plusMinutes(etaMinutes);
                    estimatedArrivalTime = eta.format(DateTimeFormatter.ofPattern("HH:mm"));
                    
                    // Calculate schedule deviation
                    scheduleDeviation = transportEtaService.calculateScheduleDeviation(
                        stop.getScheduledArrivalTime(), etaMinutes);
                    
                    // Determine ETA status
                    if (scheduleDeviation != null) {
                        if (Math.abs(scheduleDeviation) <= 5) {
                            etaStatus = "ON_TIME";
                        } else if (scheduleDeviation < -5) {
                            etaStatus = "EARLY";
                        } else {
                            etaStatus = "DELAYED";
                        }
                    } else {
                        etaStatus = "TRACKING";
                    }
                }
            }
            
            // Count students at this stop
            long studentCount = transportService.getStudentsByStopId(stop.getId()).size();
            
            StopEtaDto stopDto = StopEtaDto.builder()
                .stopId(stop.getId())
                .stopName(stop.getStopName())
                .stopOrder(stop.getStopOrder())
                .latitude(stop.getLatitude())
                .longitude(stop.getLongitude())
                .landmark(stop.getLandmark())
                .scheduledArrivalTime(stop.getScheduledArrivalTime() != null ? 
                    stop.getScheduledArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
                .estimatedArrivalMinutes(etaMinutes)
                .estimatedArrivalTime(estimatedArrivalTime)
                .scheduleDeviationMinutes(scheduleDeviation)
                .etaStatus(etaStatus)
                .studentsAssigned((int) studentCount)
                .distanceKm(distanceKm)
                .build();
            
            stopDtos.add(stopDto);
        }
        
        StopEtaDto.StopsWithEtaResponse response = StopEtaDto.StopsWithEtaResponse.builder()
            .routeId(route.getId())
            .routeName(route.getRouteName())
            .vehicleId(vehicle != null ? vehicle.getId() : null)
            .vehicleNumber(vehicle != null ? vehicle.getVehicleNumber() : null)
            .hasLiveTracking(hasLiveTracking && latestLocation != null)
            .lastUpdateTime(latestLocation != null ? latestLocation.getReceivedAt().toString() : null)
            .stops(stopDtos)
            .build();
        
        return ApiResult.success(response);
    }
    
    /**
     * Get transport events (pickup/drop history) for a student
     * STUDENT: Auto-resolves from JWT
     * PARENT: Requires ?studentId={id} with linkage validation
     */
    @Operation(summary = "Get Pickup/Drop Events", description = "Returns history of boarding/alighting events for the student.")
    @ApiResponse(responseCode = "200", description = "Events list returned")
    @GetMapping("/events")
    public ApiResult<List<TransportEventDto>> getTransportEvents(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "ID of the student (Required for PARENT role)") @RequestParam(required = false) Long studentId,
            @Parameter(description = "Date (YYYY-MM-DD)") @RequestParam(required = false) String date) {
        
        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        
        if (resolvedStudentId == null) {
            throw new BusinessException("STUDENT_RESOLUTION_FAILED", "Unable to resolve student");
        }
        
        // Parse date or default to today
        LocalDate queryDate = date != null ? 
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE) : LocalDate.now();
        
        List<TransportEvent> events = transportEventService.getEventsForStudentOnDate(
            resolvedStudentId, queryDate);
        
        List<TransportEventDto> dtos = events.stream()
            .map(this::buildTransportEventDto)
            .collect(Collectors.toList());
        
        return ApiResult.success(dtos);
    }
    
    /**
     * Ingest GPS location pings from devices
     * This endpoint uses device authentication (not JWT)
     * Should be secured with API key or device-specific auth
     */
    @Operation(summary = "Ingest GPS Ping", description = "Receives location updates from GPS hardware.")
    @SecurityRequirement(name = "api_key") // Need to define this scheme if used, or omit
    @ApiResponse(responseCode = "200", description = "Ping recorded")
    @PostMapping("/location-pings")
    public ApiResult<String> ingestLocationPing(
            @Parameter(description = "Device API Key") @RequestHeader("X-Device-API-Key") String apiKey,
            @RequestBody LocationPingDto pingDto) {
        
        // TODO: Validate API key for device authentication
        // For now, just basic validation
        
        if (pingDto.getGpsDeviceId() == null || pingDto.getLatitude() == null || pingDto.getLongitude() == null) {
            throw new BusinessException("INVALID_DATA", "Missing required GPS data fields");
        }
        
        // Find vehicle by GPS device ID
        Vehicle vehicle = transportService.getVehicleByGpsDeviceId(pingDto.getGpsDeviceId());
        
        // Build location ping entity
        LocationPing ping = new LocationPing();
        ping.setVehicle(vehicle);
        ping.setLatitude(pingDto.getLatitude());
        ping.setLongitude(pingDto.getLongitude());
        ping.setSpeedKmh(pingDto.getSpeedKmh());
        ping.setHeading(pingDto.getHeading());
        ping.setAccuracyMeters(pingDto.getAccuracyMeters());
        ping.setDeviceId(pingDto.getDeviceId());
        
        // Parse captured timestamp
        Instant capturedAt = pingDto.getCapturedAt() != null ? 
            Instant.parse(pingDto.getCapturedAt()) : Instant.now();
        ping.setCapturedAt(capturedAt);
        
        // Set trip if provided (attempt to find active trip for today)
        if (pingDto.getTripId() != null ) {
            Trip trip = transportService.getActiveTripForRoute(pingDto.getTripId(), LocalDate.now());
            if (trip != null) {
                ping.setTrip(trip);
            }
        }
        
        // Save location ping
        locationTrackingService.recordLocationPing(ping);
        
        log.info("Location ping recorded for vehicle {} (device: {})", 
            vehicle.getVehicleNumber(), pingDto.getGpsDeviceId());
        
        return ApiResult.success("Location ping recorded successfully");
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Resolve student ID with proper authorization
     * Returns null if resolution fails
     */
    private Long resolveStudentIdWithAuthorization(UserPrincipal currentUser, Long studentId) {
        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return null;
        }
        
        User user = userOpt.get();
        
        if (hasRole(user, RoleName.ROLE_STUDENT)) {
            // Auto-resolve for STUDENT
            Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
            return studentOpt.map(Student::getId).orElse(null);
            
        } else if (hasRole(user, RoleName.ROLE_PARENT)) {
            // Validate linkage for PARENT
            if (studentId == null) {
                return null;
            }
            
            Optional<Parent> parentOpt = parentService.getParentByUserId(user.getId());
            if (parentOpt.isEmpty()) {
                return null;
            }
            
            try {
                parentAuthService.validateParentStudentLinkage(parentOpt.get().getId(), studentId);
                return studentId;
            } catch (SecurityException e) {
                log.warn("Parent authorization failed: {}", e.getMessage());
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Check if user has specific role
     */
    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream()
            .anyMatch(role -> role.getName() == roleName);
    }
    
    private RouteDetailsDto.VehicleInfo buildVehicleInfo(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return RouteDetailsDto.VehicleInfo.builder()
            .vehicleId(vehicle.getId())
            .vehicleNumber(vehicle.getVehicleNumber())
            .vehicleType(vehicle.getVehicleType())
            .capacity(vehicle.getCapacity())
            .makeModel(vehicle.getMakeModel())
            .driverName(vehicle.getDriverName())
            .driverPhone(vehicle.getDriverPhone())
            .hasGpsTracking(vehicle.getGpsDeviceId() != null)
            .build();
    }
    
    private RouteDetailsDto.StopInfo buildStopInfo(Stop stop) {
        return RouteDetailsDto.StopInfo.builder()
            .stopId(stop.getId())
            .stopName(stop.getStopName())
            .stopOrder(stop.getStopOrder())
            .latitude(stop.getLatitude())
            .longitude(stop.getLongitude())
            .landmark(stop.getLandmark())
            .scheduledTime(stop.getScheduledArrivalTime() != null ? 
                stop.getScheduledArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
            .build();
    }
    
    private RouteDetailsDto.TripInfo buildTripInfo(Trip trip) {
        return RouteDetailsDto.TripInfo.builder()
            .tripId(trip.getId())
            .tripDate(trip.getTripDate().toString())
            .tripType(trip.getTripType())
            .status(trip.getStatus())
            .scheduledStartTime(trip.getScheduledStartTime() != null ? 
                trip.getScheduledStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
            .actualStartTime(trip.getActualStartTime() != null ? 
                trip.getActualStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
            .scheduledEndTime(trip.getScheduledEndTime() != null ? 
                trip.getScheduledEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
            .actualEndTime(trip.getActualEndTime() != null ? 
                trip.getActualEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
            .build();
    }
    
    private TransportEventDto buildTransportEventDto(TransportEvent event) {
        return TransportEventDto.builder()
            .eventId(event.getId())
            .tripId(event.getTrip().getId())
            .studentId(event.getStudent().getId())
            .studentName(event.getStudent().getName())
            .stopId(event.getStop() != null ? event.getStop().getId() : null)
            .stopName(event.getStop() != null ? event.getStop().getStopName() : null)
            .eventType(event.getEventType())
            .eventTimestamp(event.getEventTimestamp().toString())
            .eventTime(event.getEventTimestamp().atZone(ZoneId.systemDefault())
                .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
            .latitude(event.getLatitude())
            .longitude(event.getLongitude())
            .recordedBy(event.getRecordedBy())
            .remarks(event.getRemarks())
            .tripDate(event.getTrip().getTripDate().toString())
            .tripType(event.getTrip().getTripType())
            .routeName(event.getTrip().getRoute().getRouteName())
            .vehicleNumber(event.getTrip().getVehicle().getVehicleNumber())
            .build();
    }
}
