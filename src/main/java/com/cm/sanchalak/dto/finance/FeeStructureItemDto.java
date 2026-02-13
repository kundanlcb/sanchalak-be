package com.cm.sanchalak.dto.finance;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FeeStructureItemDto {
    private Long id;
    private Long categoryId;
    private String categoryName; // For display
    private BigDecimal amount;
}
