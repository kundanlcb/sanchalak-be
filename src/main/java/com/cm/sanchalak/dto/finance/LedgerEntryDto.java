package com.cm.sanchalak.dto.finance;

import java.math.BigDecimal;

public class LedgerEntryDto {
    private Long studentFeeMapId;
    private String structureName;
    private String academicYear;
    private BigDecimal baseAmount; // calculated from Structure Items
    private BigDecimal discountAmount;
    private BigDecimal netAmount;

    public Long getStudentFeeMapId() { return studentFeeMapId; }
    public String getStructureName() { return structureName; }
    public String getAcademicYear() { return academicYear; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getNetAmount() { return netAmount; }

    public void setStudentFeeMapId(Long studentFeeMapId) { this.studentFeeMapId = studentFeeMapId; }
    public void setStructureName(String structureName) { this.structureName = structureName; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
}
