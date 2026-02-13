package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fee ledger DTO for student/parent portal
 * Shows fee structure, payments, and outstanding balance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeLedgerDto {
    
    private Long studentId;
    
    private String studentName;
    
    private String academicYear;
    
    private BigDecimal totalFees;
    
    private BigDecimal paidAmount;
    
    private BigDecimal outstandingBalance;
    
    private List<FeeItemDto> feeBreakdown;
    
    private List<PaymentDto> paymentHistory;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeItemDto {
        
        private String feeType; // TUITION, TRANSPORT, LIBRARY, SPORTS, etc.
        
        private BigDecimal amount;
        
        private String dueDate;
        
        private String status; // PAID, PENDING, OVERDUE
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDto {
        
        private Long paymentId;
        
        private String paymentDate;
        
        private BigDecimal amount;
        
        private String paymentMode; // CASH, ONLINE, CHEQUE, etc.
        
        private String receiptNumber;
        
        private String transactionId;
    }
}
