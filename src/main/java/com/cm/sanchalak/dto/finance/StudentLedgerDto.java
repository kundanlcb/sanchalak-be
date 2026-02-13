package com.cm.sanchalak.dto.finance;

import java.math.BigDecimal;
import java.util.List;

public class StudentLedgerDto {
    private Long studentId;
    private BigDecimal totalDues;
    private BigDecimal totalPaid;
    private BigDecimal pendingBalance;
    private BigDecimal lateFees;
    
    private List<LedgerEntryDto> dues;
    private List<PaymentTransactionDto> transactions;

    public Long getStudentId() { return studentId; }
    public BigDecimal getTotalDues() { return totalDues; }
    public BigDecimal getTotalPaid() { return totalPaid; }
    public BigDecimal getPendingBalance() { return pendingBalance; }
    public BigDecimal getLateFees() { return lateFees; }
    public List<LedgerEntryDto> getDues() { return dues; }
    public List<PaymentTransactionDto> getTransactions() { return transactions; }

    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public void setTotalDues(BigDecimal totalDues) { this.totalDues = totalDues; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
    public void setPendingBalance(BigDecimal pendingBalance) { this.pendingBalance = pendingBalance; }
    public void setLateFees(BigDecimal lateFees) { this.lateFees = lateFees; }
    public void setDues(List<LedgerEntryDto> dues) { this.dues = dues; }
    public void setTransactions(List<PaymentTransactionDto> transactions) { this.transactions = transactions; }
}
