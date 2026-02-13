package com.cm.sanchalak.dto.finance;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentTransactionDto {
    private Long id;
    private Long studentId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionReference;
    private String status;
    private LocalDateTime paymentDate;
}
