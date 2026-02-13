package com.cm.sanchalak.dto.finance;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StudentLedgerDto {
    private Long studentId;
    private BigDecimal totalDues;
    private BigDecimal totalPaid;
    private BigDecimal pendingBalance;
    private BigDecimal lateFees;
    
    private List<LedgerEntryDto> dues;
    private List<PaymentTransactionDto> transactions;
}
