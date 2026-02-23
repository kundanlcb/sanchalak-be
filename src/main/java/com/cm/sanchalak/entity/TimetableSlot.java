package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "timetable_slots", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "school_id", "order_index" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name; // e.g., "Period 1", "Morning Assembly", "Lunch Break"

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Column(name = "is_break", nullable = false)
    private Boolean isBreak; // Distinguishes between instructional time and breaks

    @NotNull
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex; // Determines the display order

    @NotNull
    @Column(name = "school_id", nullable = false)
    private java.util.UUID schoolId;
}
