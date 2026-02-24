package com.cm.sanchalak.dto.finance;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandBillResponse {

    private Long studentId;
    private String studentName;
    private String fatherName;
    private String className;
    private String rollNo;
    private String admissionNumber;
    private String billNo;
    private String billDate;
    private String monthLabel;

    private List<LineItemResponse> lineItems;

    private BigDecimal totalCurrentFees;
    private BigDecimal totalBackDues;
    private BigDecimal grandTotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItemResponse {
        private String categoryName;
        private String monthsUpto;
        private BigDecimal amount;
        private Boolean isBackDue;
    }
}
