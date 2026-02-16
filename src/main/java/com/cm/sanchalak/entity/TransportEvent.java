package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity for transport events (pickup, drop, absence)
 */
@Entity
@Table(name = "transport_events", indexes = {
        @Index(name = "idx_event_trip_student", columnList = "trip_id, student_id"),
        @Index(name = "idx_event_student_date", columnList = "student_id, event_timestamp DESC"),
        @Index(name = "idx_event_type", columnList = "event_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class TransportEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "stop_id")
    private Stop stop;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType; // PICKED_UP, DROPPED_OFF, ABSENT, CANCELLED

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy; // Driver name or system

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
