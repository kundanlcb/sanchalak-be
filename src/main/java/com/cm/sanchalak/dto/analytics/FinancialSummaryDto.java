package com.cm.sanchalak.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancialSummaryDto {
    private Double totalExpectedRevenue;
    private Double totalCollected;
    private Double totalOutstanding;
    private int totalTransactions;
}
