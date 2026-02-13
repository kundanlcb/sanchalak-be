package com.cm.sanchalak.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionTrendDto {
    private LocalDate date;
    private BigDecimal amount;
    private Long transactionCount;
}
