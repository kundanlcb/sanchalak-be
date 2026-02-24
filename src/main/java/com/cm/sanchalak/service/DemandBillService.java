package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.finance.DemandBillRequest;
import com.cm.sanchalak.dto.finance.DemandBillResponse;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.security.SchoolContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandBillService {

    private final DemandBillRepository demandBillRepository;
    private final StudentRepository studentRepository;
    private final DocumentTemplateRepository documentTemplateRepository;
    private final ReceiptService receiptService;

    /**
     * Preview demand bill data without saving — used to show admin the bill before
     * generating.
     */
    @Transactional(readOnly = true)
    public List<DemandBillResponse> preview(DemandBillRequest request) {
        UUID schoolId = SchoolContext.getSchoolId();
        List<Student> students = getStudentsForRequest(request, schoolId);
        return students.stream()
                .map(s -> buildBillResponse(s, request, schoolId))
                .collect(Collectors.toList());
    }

    /**
     * Generate and save demand bills, return combined PDF bytes (2-up per A4 page).
     */
    @Transactional
    public byte[] generatePdf(DemandBillRequest request) {
        UUID schoolId = SchoolContext.getSchoolId();
        List<Student> students = getStudentsForRequest(request, schoolId);
        List<DemandBillResponse> bills = new ArrayList<>();

        for (Student student : students) {
            DemandBillResponse billData = buildBillResponse(student, request, schoolId);

            // Save to DB
            DemandBill bill = DemandBill.builder()
                    .schoolId(schoolId)
                    .student(student)
                    .billNo(billData.getBillNo())
                    .billDate(LocalDate.now())
                    .monthLabel(request.getMonthLabel())
                    .totalCurrentFees(billData.getTotalCurrentFees())
                    .totalBackDues(billData.getTotalBackDues())
                    .grandTotal(billData.getGrandTotal())
                    .build();

            List<DemandBillLineItem> lineItems = billData.getLineItems().stream().map(li -> DemandBillLineItem.builder()
                    .demandBill(bill)
                    .categoryName(li.getCategoryName())
                    .monthsUpto(li.getMonthsUpto())
                    .amount(li.getAmount())
                    .isBackDue(li.getIsBackDue() != null && li.getIsBackDue())
                    .build()).collect(Collectors.toList());
            bill.setLineItems(lineItems);
            demandBillRepository.save(bill);
            bills.add(billData);
        }

        // Get school template (safe fallback)
        DocumentTemplate template = documentTemplateRepository.findBySchoolId(schoolId).orElse(null);

        Map<String, Object> data = new HashMap<>();
        data.put("bills", bills);
        data.put("template", template);
        data.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return receiptService.generatePdf("demand-bill", data);
    }

    /**
     * Get bill history for a student.
     */
    @Transactional(readOnly = true)
    public List<DemandBillResponse> getStudentHistory(Long studentId) {
        return demandBillRepository.findByStudentIdOrderByBillDateDesc(studentId)
                .stream().map(this::mapBillToResponse).collect(Collectors.toList());
    }

    // ---- Private helpers ----

    private List<Student> getStudentsForRequest(DemandBillRequest request, UUID schoolId) {
        if (request.getClassId() != null) {
            return studentRepository.findAll().stream()
                    .filter(s -> !s.isDeleted()
                            && schoolId.equals(s.getSchoolId())
                            && s.getStudentClass() != null
                            && s.getStudentClass().getId().equals(request.getClassId()))
                    .collect(Collectors.toList());
        }
        // All students of this school
        return studentRepository.findAll().stream()
                .filter(s -> !s.isDeleted() && schoolId.equals(s.getSchoolId()))
                .collect(Collectors.toList());
    }

    private DemandBillResponse buildBillResponse(Student student, DemandBillRequest request, UUID schoolId) {
        // Compute back dues — check total paid vs total assigned
        // This is the foundation; will be refined as monthly payment tracking is added
        BigDecimal currentFees = request.getLineItems().stream()
                .map(DemandBillRequest.LineItemRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Back dues = any outstanding balance from previous months
        // For simplicity: pull from ledger. This will be refined as payments are
        // tracked monthly.
        BigDecimal backDues = request.getBackDues() != null ? request.getBackDues() : BigDecimal.ZERO;

        BigDecimal grandTotal = currentFees.add(backDues);

        String billNo = generateBillNo(schoolId, request.getMonthLabel());

        List<DemandBillResponse.LineItemResponse> lineItems = request.getLineItems().stream()
                .map(li -> DemandBillResponse.LineItemResponse.builder()
                        .categoryName(li.getCategoryName())
                        .monthsUpto(request.getMonthLabel())
                        .amount(li.getAmount())
                        .isBackDue(false)
                        .build())
                .collect(Collectors.toList());

        // Add back due breakdown lines if provided
        if (request.getBackDueBreakdown() != null) {
            request.getBackDueBreakdown().forEach(bk -> lineItems.add(
                    DemandBillResponse.LineItemResponse.builder()
                            .categoryName(bk.getLabel())
                            .monthsUpto(bk.getPeriod())
                            .amount(bk.getAmount())
                            .isBackDue(true)
                            .build()));
        }

        return DemandBillResponse.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .fatherName(student.getFatherName() != null ? student.getFatherName() : student.getGuardianName())
                .className(student.getStudentClass() != null ? student.getStudentClass().getName() : "")
                .rollNo(student.getRollNo() != null ? student.getRollNo().toString() : "")
                .admissionNumber(student.getAdmissionNumber())
                .billNo(billNo)
                .billDate(LocalDate.now().toString())
                .monthLabel(request.getMonthLabel())
                .lineItems(lineItems)
                .totalCurrentFees(currentFees)
                .totalBackDues(backDues)
                .grandTotal(grandTotal)
                .build();
    }

    private String generateBillNo(UUID schoolId, String monthLabel) {
        int seq = demandBillRepository.countBySchoolIdAndMonthLabel(schoolId, monthLabel) + 1;
        String prefix = monthLabel.replaceAll("\\s+", "-").toUpperCase();
        return String.format("%s-%03d", prefix, seq);
    }

    private DemandBillResponse mapBillToResponse(DemandBill bill) {
        List<DemandBillResponse.LineItemResponse> items = bill.getLineItems().stream()
                .map(li -> DemandBillResponse.LineItemResponse.builder()
                        .categoryName(li.getCategoryName())
                        .monthsUpto(li.getMonthsUpto())
                        .amount(li.getAmount())
                        .isBackDue(li.getIsBackDue())
                        .build())
                .collect(Collectors.toList());

        return DemandBillResponse.builder()
                .studentId(bill.getStudent().getId())
                .studentName(bill.getStudent().getName())
                .billNo(bill.getBillNo())
                .billDate(bill.getBillDate().toString())
                .monthLabel(bill.getMonthLabel())
                .lineItems(items)
                .totalCurrentFees(bill.getTotalCurrentFees())
                .totalBackDues(bill.getTotalBackDues())
                .grandTotal(bill.getGrandTotal())
                .build();
    }
}
