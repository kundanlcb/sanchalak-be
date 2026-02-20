package com.cm.sanchalak.platform.subscription;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class SubscriptionPlanRequest {
    private String name;
    private BigDecimal price;
    private int durationMonths;
    private Integer maxStudents;
    private List<UUID> featureIds;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public List<UUID> getFeatureIds() {
        return featureIds;
    }

    public void setFeatureIds(List<UUID> featureIds) {
        this.featureIds = featureIds;
    }
}
