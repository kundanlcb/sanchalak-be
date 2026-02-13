package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.finance.StudentLedgerDto;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FinanceServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentFeeMapRepository studentFeeMapRepository;
    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;
    @Mock
    private FeeCategoryRepository feeCategoryRepository;
    @Mock
    private FeeStructureRepository feeStructureRepository;
    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private ReceiptService receiptService;

    @InjectMocks
    private FinanceService financeService;

    @Test
    void testGetStudentLedger() {
        Long studentId = 1L;
        
        when(studentRepository.existsById(studentId)).thenReturn(true);

        // Fee Structure
        FeeStructure structure = new FeeStructure();
        structure.setName("Annual Fee");
        structure.setAcademicYear("2024-25");
        structure.setLateFeeAmount(new BigDecimal("100"));
        structure.setGracePeriodDays(30);

        FeeStructureItem item = new FeeStructureItem();
        item.setAmount(new BigDecimal("1000"));
        structure.setItems(Collections.singletonList(item));

        // Map
        StudentFeeMap map = new StudentFeeMap();
        map.setId(10L);
        map.setFeeStructure(structure);
        map.setCreatedAt(Instant.now().minus(40, ChronoUnit.DAYS)); // Late fee applies
        // Assuming discount 0
        
        when(studentFeeMapRepository.findByStudentId(studentId)).thenReturn(Arrays.asList(map));

        // Transaction
        Student student = new Student();
        student.setId(studentId);

        PaymentTransaction tx = new PaymentTransaction();
        tx.setStudent(student);
        tx.setAmount(new BigDecimal("500"));
        tx.setStatus("SUCCESS");
        
        when(paymentTransactionRepository.findByStudentId(studentId)).thenReturn(Arrays.asList(tx));

        StudentLedgerDto ledger = financeService.getStudentLedger(studentId);

        // Assertions
        assertNotNull(ledger);
        // Base: 1000, Late: 100 (grace period 30, passed 40 days)
        assertEquals(new BigDecimal("1100"), ledger.getTotalDues());
        assertEquals(new BigDecimal("500"), ledger.getTotalPaid());
        assertEquals(new BigDecimal("600"), ledger.getPendingBalance());
        assertEquals(new BigDecimal("100"), ledger.getLateFees());
    }
}
