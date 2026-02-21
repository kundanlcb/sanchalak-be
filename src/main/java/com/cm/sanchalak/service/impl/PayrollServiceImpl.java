package com.cm.sanchalak.service.impl;

import com.cm.sanchalak.dto.finance.PayrollRecordDto;
import com.cm.sanchalak.dto.finance.PayrollSummaryDto;
import com.cm.sanchalak.service.PayrollService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.cm.sanchalak.entity.PayrollRecord;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.repository.PayrollRecordRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRecordRepository payrollRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public List<PayrollRecordDto> getPayrollHistory() {
        List<PayrollRecord> records = payrollRepository.findAllByOrderByPaidAtDesc();
        List<PayrollRecordDto> history = new ArrayList<>();

        for (PayrollRecord pr : records) {
            history.add(PayrollRecordDto.builder()
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
                    .build());
        }

        return history;
    }

    @Override
    public PayrollSummaryDto getPayrollSummary() {
        List<PayrollRecord> allRecords = payrollRepository.findAll();

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

        long totalStaffCount = teacherRepository.count();

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
        List<Teacher> activeTeachers = teacherRepository.findAll();

        for (Teacher teacher : activeTeachers) {
            // Check if already generated
            boolean exists = payrollRepository.findByTeacherIdAndMonth(teacher.getId(), month).isPresent();
            if (!exists) {
                PayrollRecord record = new PayrollRecord();
                record.setTeacher(teacher);
                record.setMonth(month);

                // Real logic would calculate these based on the teacher's profile/attendance.
                // Using generic defaults for the assignment:
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
}
