package com.cm.sanchalak.dto.finance;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DefaulterDto {
    private Long id; // Student ID
    private String studentName;
    private String studentId; // String representation or Roll No? Frontend expects string. Let's use Roll No
                              // or ID string
    private String grade;
    private BigDecimal amountDue;
    private Integer daysOverdue;
}
