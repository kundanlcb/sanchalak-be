package com.cm.sanchalak.platform.school;

import com.cm.sanchalak.entity.BaseEntity;
import com.cm.sanchalak.platform.subscription.Feature;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "school_feature_entitlements", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "school_id", "feature_id" })
})
public class SchoolFeatureEntitlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "source_plan_id")
    private UUID sourcePlanId;

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

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public UUID getSourcePlanId() {
        return sourcePlanId;
    }

    public void setSourcePlanId(UUID sourcePlanId) {
        this.sourcePlanId = sourcePlanId;
    }
}
