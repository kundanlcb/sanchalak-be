package com.cm.sanchalak.dto.finance;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LedgerEntryDto {
    private Long studentFeeMapId;
    private String structureName;
    private String academicYear;
    private BigDecimal baseAmount; // calculated from Structure Items
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
}
