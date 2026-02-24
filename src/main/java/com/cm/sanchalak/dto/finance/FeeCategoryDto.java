package com.cm.sanchalak.dto.finance;

import jakarta.validation.constraints.NotBlank;

public class FeeCategoryDto {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private String type;
    private String frequency;
    private Boolean isMandatory;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getFrequency() {
        return frequency;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }
}
