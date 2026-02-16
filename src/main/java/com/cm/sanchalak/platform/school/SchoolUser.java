package com.cm.sanchalak.platform.school;

import com.cm.sanchalak.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "school_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "school_id", "user_id" })
})
public class SchoolUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    public SchoolUser() {
    }

    public SchoolUser(UUID schoolId, UUID userId) {
        this.schoolId = schoolId;
        this.userId = userId;
    }

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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
