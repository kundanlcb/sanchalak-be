package com.cm.sanchalak.dto.finance;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public class FeeStructureDto {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Academic year is required")
    private String academicYear;
    @NotBlank(message = "Frequency is required")
    private String frequency;
    private BigDecimal lateFeeAmount;
    private Integer gracePeriodDays;
    private List<FeeStructureItemDto> items;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAcademicYear() { return academicYear; }
    public String getFrequency() { return frequency; }
    public BigDecimal getLateFeeAmount() { return lateFeeAmount; }
    public Integer getGracePeriodDays() { return gracePeriodDays; }
    public List<FeeStructureItemDto> getItems() { return items; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setLateFeeAmount(BigDecimal lateFeeAmount) { this.lateFeeAmount = lateFeeAmount; }
    public void setGracePeriodDays(Integer gracePeriodDays) { this.gracePeriodDays = gracePeriodDays; }
    public void setItems(List<FeeStructureItemDto> items) { this.items = items; }
}
