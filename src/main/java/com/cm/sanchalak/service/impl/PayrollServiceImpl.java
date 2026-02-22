package com.cm.sanchalak.service.impl;

import com.cm.sanchalak.dto.finance.PayrollRecordDto;
import com.cm.sanchalak.dto.finance.PayrollSummaryDto;
import com.cm.sanchalak.entity.PayrollRecord;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.repository.PayrollRecordRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.spec.PayrollSpecification;
import com.cm.sanchalak.repository.spec.TeacherSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRecordRepository payrollRepository;
    private final TeacherRepository teacherRepository;
    private final OwnershipValidator ownership;

    @Override
    @Transactional(readOnly = true)
    public List<PayrollRecordDto> getPayrollHistory() {
        List<PayrollRecord> records = payrollRepository.findAll(PayrollSpecification.activeScoped());

        return records.stream()
                .sorted((r1, r2) -> {
                    if (r1.getPaidAt() == null && r2.getPaidAt() == null)
                        return 0;
                    if (r1.getPaidAt() == null)
                        return 1;
                    if (r2.getPaidAt() == null)
                        return -1;
                    return r2.getPaidAt().compareTo(r1.getPaidAt());
                })
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollSummaryDto getPayrollSummary() {
        List<PayrollRecord> allRecords = payrollRepository.findAll(PayrollSpecification.activeScoped());

        BigDecimal totalPayout = BigDecimal.ZERO;
        int paidStaff = 0;
        int pendingStaff = 0;

        for (PayrollRecord pr : allRecords) {
            if ("PAID".equalsIgnoreCase(pr.getStatus())) {
                totalPayout = totalPayout.add(BigDecimal.valueOf(pr.getNetSalary()));
                paidStaff++;
            } else {
                pendingStaff++;
            }
        }

        long totalStaffCount = teacherRepository.count(TeacherSpecification.activeScoped());

        return PayrollSummaryDto.builder()
                .totalPayout(totalPayout)
                .totalStaff((int) totalStaffCount)
                .paidStaff(paidStaff)
                .pendingStaff(pendingStaff)
                .lastGenerated(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                .build();
    }

    @Override
    public void generatePayroll(String month) {
        List<Teacher> activeTeachers = teacherRepository.findAll(TeacherSpecification.activeScoped());

        for (Teacher teacher : activeTeachers) {
            boolean exists = payrollRepository.findOne(PayrollSpecification.activeScoped()
                    .and((root, query, cb) -> cb.equal(root.get("teacher").get("id"), teacher.getId()))
                    .and((root, query, cb) -> cb.equal(root.get("month"), month)))
                    .isPresent();

            if (!exists) {
                PayrollRecord record = new PayrollRecord();
                record.setTeacher(teacher);
                record.setMonth(month);

                // Default logic as per existing implementation
                Double basicPay = 50000.0;
                Double allowances = 5000.0;
                Double deductions = 2000.0;
                Double netSalary = (basicPay + allowances) - deductions;

                record.setBasicPay(basicPay);
                record.setAllowances(allowances);
                record.setDeductions(deductions);
                record.setNetSalary(netSalary);
                record.setStatus("GENERATED");

                payrollRepository.save(record);
            }
        }
    }

    private PayrollRecordDto mapToDto(PayrollRecord pr) {
        return PayrollRecordDto.builder()
                .id("PRL-" + String.format("%03d", pr.getId()))
                .staffId("TCH-" + String.format("%03d", pr.getTeacher().getId()))
                .staffName(pr.getTeacher().getName())
                .month(pr.getMonth())
                .basicPay(BigDecimal.valueOf(pr.getBasicPay()))
                .allowance(BigDecimal.valueOf(pr.getAllowances()))
                .deduction(BigDecimal.valueOf(pr.getDeductions()))
                .netSalary(BigDecimal.valueOf(pr.getNetSalary()))
                .status(pr.getStatus())
                .paidAt(pr.getPaidAt() != null ? pr.getPaidAt().format(DateTimeFormatter.ISO_DATE_TIME) : null)
                .build();
    }
}
