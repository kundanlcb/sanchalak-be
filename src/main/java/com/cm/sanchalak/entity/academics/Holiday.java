package com.cm.sanchalak.entity.academics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "holidays")
@Getter
@Setter
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Multi-tenant isolation. Can be NULL for System-wide National Holidays.
    @Column(name = "tenant_id", nullable = true)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HolidayType type;

    @Column(nullable = false)
    private boolean applicableToStudents;

    @Column(nullable = false)
    private boolean applicableToStaff;

    @Column(nullable = false)
    private String academicYear;
}
