package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.TransportEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransportEventRepository
              extends JpaRepository<TransportEvent, Long>, JpaSpecificationExecutor<TransportEvent> {

       @Query("SELECT te FROM TransportEvent te " +
                     "WHERE te.trip.id = :tripId AND te.student.id = :studentId " +
                     "ORDER BY te.eventTimestamp DESC")
       List<TransportEvent> findByTripIdAndStudentId(Long tripId, Long studentId);

       @Query("SELECT te FROM TransportEvent te " +
                     "WHERE te.trip.id = :tripId AND te.eventType = :eventType " +
                     "ORDER BY te.eventTimestamp")
       List<TransportEvent> findByTripIdAndEventType(Long tripId, String eventType);

       @Query("SELECT te FROM TransportEvent te " +
                     "WHERE te.student.id = :studentId " +
                     "AND te.eventTimestamp >= :startTime AND te.eventTimestamp < :endTime " +
                     "ORDER BY te.eventTimestamp DESC")
       List<TransportEvent> findByStudentIdAndDateRange(Long studentId, Instant startTime, Instant endTime);

       @Query("SELECT te FROM TransportEvent te " +
                     "WHERE te.trip.id = :tripId " +
                     "ORDER BY te.eventTimestamp")
       List<TransportEvent> findByTripId(Long tripId);

       Optional<TransportEvent> findFirstByTripIdAndStudentIdAndEventType(Long tripId, Long studentId,
                     String eventType);
}
