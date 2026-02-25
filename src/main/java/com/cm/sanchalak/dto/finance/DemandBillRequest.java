package com.cm.sanchalak.dto.finance;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandBillRequest {

    private String monthLabel; // e.g. "JUNE 2025"
    private Long classId; // optional — null means all classes
    private Long studentId; // optional — if set, generates for one student only
    private BigDecimal backDues; // total back dues (optional, computed from ledger)

    private List<LineItemRequest> lineItems;
    private List<BackDueBreakdown> backDueBreakdown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItemRequest {
        private String categoryName;
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BackDueBreakdown {
        private String label; // e.g. "Tuition fee (Apr)"
        private String period; // e.g. "April 2025"
        private BigDecimal amount;
    }
}
