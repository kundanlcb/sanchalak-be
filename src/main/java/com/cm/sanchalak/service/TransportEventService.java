package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.TransportEvent;
import com.cm.sanchalak.repository.TransportEventRepository;
import com.cm.sanchalak.repository.spec.StudentSpecification;
import com.cm.sanchalak.repository.spec.TransportEventSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
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
    private final OwnershipValidator ownership;

    /**
     * Get transport events for a student on a specific date
     */
    @Transactional(readOnly = true)
    public List<TransportEvent> getEventsForStudentOnDate(Long studentId, LocalDate date) {
        Instant startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return transportEventRepository.findAll(TransportEventSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                .and((root, query, cb) -> cb.between(root.get("eventTimestamp"), startTime, endTime)),
                org.springframework.data.domain.Sort.by("eventTimestamp").descending());
    }

    /**
     * Get transport events for a student within a date range
     */
    @Transactional(readOnly = true)
    public List<TransportEvent> getEventsForStudentInDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        Instant startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return transportEventRepository.findAll(TransportEventSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                .and((root, query, cb) -> cb.between(root.get("eventTimestamp"), startTime, endTime)),
                org.springframework.data.domain.Sort.by("eventTimestamp").descending());
    }

    /**
     * Get all events for a trip
     */
    @Transactional(readOnly = true)
    public List<TransportEvent> getEventsForTrip(Long tripId) {
        // Trip scoping is checked implicitly via student association in
        // TransportEventSpecification
        // or we can add trip scoping if Trip has schoolId.
        return transportEventRepository.findAll(TransportEventSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("trip").get("id"), tripId)),
                org.springframework.data.domain.Sort.by("eventTimestamp").ascending());
    }

    /**
     * Get events for a specific trip and student
     */
    @Transactional(readOnly = true)
    public List<TransportEvent> getEventsForTripAndStudent(Long tripId, Long studentId) {
        return transportEventRepository.findAll(TransportEventSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("trip").get("id"), tripId))
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId)),
                org.springframework.data.domain.Sort.by("eventTimestamp").descending());
    }

    /**
     * Record a new transport event
     */
    @Transactional
    public TransportEvent recordEvent(TransportEvent event) {
        // Validate ownership of student and trip
        ownership.validate(event.getStudent().getSchoolId());

        TransportEvent saved = transportEventRepository.save(event);
        log.info("Recorded transport event: {} for student {} on trip {}",
                event.getEventType(), event.getStudent().getId(), event.getTrip().getId());
        return saved;
    }

    /**
     * Check if student was picked up on a trip
     */
    @Transactional(readOnly = true)
    public boolean wasStudentPickedUp(Long tripId, Long studentId) {
        return transportEventRepository.findOne(TransportEventSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("trip").get("id"), tripId))
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                .and((root, query, cb) -> cb.equal(root.get("eventType"), "PICKED_UP")))
                .isPresent();
    }

    /**
     * Check if student was dropped off on a trip
     */
    @Transactional(readOnly = true)
    public boolean wasStudentDroppedOff(Long tripId, Long studentId) {
        return transportEventRepository.findOne(TransportEventSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("trip").get("id"), tripId))
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                .and((root, query, cb) -> cb.equal(root.get("eventType"), "DROPPED_OFF")))
                .isPresent();
    }
}
