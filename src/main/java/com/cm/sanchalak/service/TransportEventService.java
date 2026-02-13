package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.TransportEvent;
import com.cm.sanchalak.repository.TransportEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Service for transport events (pickup, drop, absence tracking)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransportEventService {
    
    private final TransportEventRepository transportEventRepository;
    
    /**
     * Get transport events for a student on a specific date
     */
    public List<TransportEvent> getEventsForStudentOnDate(Long studentId, LocalDate date) {
        Instant startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        return transportEventRepository.findByStudentIdAndDateRange(studentId, startTime, endTime);
    }
    
    /**
     * Get transport events for a student within a date range
     */
    public List<TransportEvent> getEventsForStudentInDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        Instant startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        return transportEventRepository.findByStudentIdAndDateRange(studentId, startTime, endTime);
    }
    
    /**
     * Get all events for a trip
     */
    public List<TransportEvent> getEventsForTrip(Long tripId) {
        return transportEventRepository.findByTripId(tripId);
    }
    
    /**
     * Get events for a specific trip and student
     */
    public List<TransportEvent> getEventsForTripAndStudent(Long tripId, Long studentId) {
        return transportEventRepository.findByTripIdAndStudentId(tripId, studentId);
    }
    
    /**
     * Record a new transport event
     */
    @Transactional
    public TransportEvent recordEvent(TransportEvent event) {
        TransportEvent saved = transportEventRepository.save(event);
        log.info("Recorded transport event: {} for student {} on trip {}", 
            event.getEventType(), event.getStudent().getId(), event.getTrip().getId());
        return saved;
    }
    
    /**
     * Check if student was picked up on a trip
     */
    public boolean wasStudentPickedUp(Long tripId, Long studentId) {
        return transportEventRepository.findFirstByTripIdAndStudentIdAndEventType(
            tripId, studentId, "PICKED_UP"
        ).isPresent();
    }
    
    /**
     * Check if student was dropped off on a trip
     */
    public boolean wasStudentDroppedOff(Long tripId, Long studentId) {
        return transportEventRepository.findFirstByTripIdAndStudentIdAndEventType(
            tripId, studentId, "DROPPED_OFF"
        ).isPresent();
    }
}
