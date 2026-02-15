package com.cm.sanchalak.platform.operations;

import com.cm.sanchalak.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "school_operation_configs")
public class SchoolOperationConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "school_id", nullable = false, unique = true)
    private UUID schoolId;

    @Column(name = "attendance_enabled")
    private boolean attendanceEnabled = true;

    @Column(name = "notices_enabled")
    private boolean noticesEnabled = true;

    @Column(name = "routine_enabled")
    private boolean routineEnabled = true;

    // Week structure (e.g., 0=Sunday is holiday, etc.) - simplified as JSON or
    // boolean flags
    @Column(name = "saturday_is_working")
    private boolean saturdayIsWorking = false;

    public SchoolOperationConfig() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public boolean isAttendanceEnabled() {
        return attendanceEnabled;
    }

    public void setAttendanceEnabled(boolean attendanceEnabled) {
        this.attendanceEnabled = attendanceEnabled;
    }

    public boolean isNoticesEnabled() {
        return noticesEnabled;
    }

    public void setNoticesEnabled(boolean noticesEnabled) {
        this.noticesEnabled = noticesEnabled;
    }

    public boolean isRoutineEnabled() {
        return routineEnabled;
    }

    public void setRoutineEnabled(boolean routineEnabled) {
        this.routineEnabled = routineEnabled;
    }

    public boolean isSaturdayIsWorking() {
        return saturdayIsWorking;
    }

    public void setSaturdayIsWorking(boolean saturdayIsWorking) {
        this.saturdayIsWorking = saturdayIsWorking;
    }
}
