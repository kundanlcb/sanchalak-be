package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity representing student transport assignment
 */
@Entity
@Table(name = "student_transport_assignments", indexes = {
        @Index(name = "idx_transport_student", columnList = "student_id"),
        @Index(name = "idx_transport_route", columnList = "route_id"),
        @Index(name = "idx_transport_stop", columnList = "stop_id"),
        @Index(name = "idx_transport_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StudentTransportAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "assignment_type", length = 20)
    private String assignmentType; // BOTH, PICKUP_ONLY, DROP_ONLY

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
