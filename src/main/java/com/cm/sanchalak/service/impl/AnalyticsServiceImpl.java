package com.cm.sanchalak.service.impl;

import com.cm.sanchalak.dto.analytics.*;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.repository.spec.ExamTermSpecification;
import com.cm.sanchalak.repository.spec.StudentMarksSpecification;
import com.cm.sanchalak.repository.spec.StudentSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

        private final StudentRepository studentRepository;
        private final ExamTermRepository examTermRepository;
        private final AttendanceRepository attendanceRepository;
        private final StudentMarksRepository studentMarksRepository;
        private final PaymentTransactionRepository paymentTransactionRepository;
        private final StudentFeeMapRepository studentFeeMapRepository;
        private final OwnershipValidator ownership;

        @Override
        @Transactional(readOnly = true)
        public ReportCardDataDto getReportCardData(Long studentId, Long termId) {
                Student student = studentRepository.findOne(StudentSpecification.activeById(studentId))
                                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

                ExamTerm term = examTermRepository.findOne(ExamTermSpecification.activeById(termId))
                                .orElseThrow(() -> new RuntimeException("Term not found with id: " + termId));

                // 1. Calculate Attendance
                AttendanceSummaryDto attendance = calculateAttendance(studentId, term);

                // 2. Fetch and Process Marks
                List<StudentMarks> marksEntities = studentMarksRepository.findAll(StudentMarksSpecification
                                .activeScoped()
                                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                                .and((root, query, cb) -> cb.equal(root.get("examSchedule").get("examTerm").get("id"),
                                                termId)));

                List<SubjectMarkDto> academics = marksEntities.stream()
                                .map(this::mapToSubjectMarkDto)
                                .collect(Collectors.toList());

                // 3. Compute Result Summary
                ReportCardDataDto.ResultSummaryDto result = calculateResultSummary(academics);

                // 4. Build Profile
                ReportCardDataDto.StudentProfileDto profile = ReportCardDataDto.StudentProfileDto.builder()
                                .name(student.getName())
                                .excludeNumber(String.valueOf(student.getId()))
                                .className(student.getStudentClass() != null ? student.getStudentClass().getName()
                                                : "N/A")
                                .section("N/A")
                                .build();

                // 5. Build Term Details
                ReportCardDataDto.TermDetailsDto termDetails = ReportCardDataDto.TermDetailsDto.builder()
                                .name(term.getName())
                                .session(generateSession(term))
                                .build();

                return ReportCardDataDto.builder()
                                .student(profile)
                                .term(termDetails)
                                .attendance(attendance)
                                .academics(academics)
                                .result(result)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public FinancialSummaryDto getFinancialSummary() {
                UUID schoolId = SchoolContext.getSchoolId();
                ownership.validate(schoolId);

                BigDecimal baseFee = studentFeeMapRepository.sumTotalBaseFee(schoolId);
                if (baseFee == null)
                        baseFee = BigDecimal.ZERO;

                BigDecimal discounts = studentFeeMapRepository.sumTotalDiscounts(schoolId);
                if (discounts == null)
                        discounts = BigDecimal.ZERO;

                BigDecimal expected = baseFee.subtract(discounts);

                BigDecimal collected = paymentTransactionRepository.sumTotalCollected(schoolId);
                if (collected == null)
                        collected = BigDecimal.ZERO;

                BigDecimal outstanding = expected.subtract(collected);

                long count = paymentTransactionRepository
                                .count((root, query, cb) -> cb.equal(root.get("schoolId"), schoolId));

                return FinancialSummaryDto.builder()
                                .totalExpectedRevenue(expected.doubleValue())
                                .totalCollected(collected.doubleValue())
                                .totalOutstanding(outstanding.doubleValue())
                                .totalTransactions((int) count)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public List<CollectionTrendDto> getCollectionTrend(int days) {
                UUID schoolId = SchoolContext.getSchoolId();
                ownership.validate(schoolId);

                LocalDateTime end = LocalDateTime.now();
                LocalDateTime start = end.minusDays(days);

                return paymentTransactionRepository.findCollectionTrend(start, end, schoolId);
        }

        private AttendanceSummaryDto calculateAttendance(Long studentId, ExamTerm term) {
                // Find records within term dates
                List<AttendanceRecord> records = attendanceRepository.findByStudentIdAndDateBetween(
                                studentId, term.getStartDate(), term.getEndDate());

                // Filter out HOLIDAYS from total working days
                List<AttendanceRecord> workingDays = records.stream()
                                .filter(r -> r.getStatus() != AttendanceStatus.HOLIDAY)
                                .collect(Collectors.toList());

                int totalDays = workingDays.size();

                // Count Present (PRESENT or LATE)
                int presentDays = (int) workingDays.stream()
                                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT
                                                || r.getStatus() == AttendanceStatus.LATE)
                                .count();

                double percentage = totalDays > 0 ? ((double) presentDays / totalDays) * 100 : 0.0;
                // Round to 1 decimal place
                percentage = Math.round(percentage * 10.0) / 10.0;

                return AttendanceSummaryDto.builder()
                                .totalDays(totalDays)
                                .presentDays(presentDays)
                                .percentage(percentage)
                                .build();
        }

        private SubjectMarkDto mapToSubjectMarkDto(StudentMarks sm) {
                String subjectName = sm.getExamSchedule().getSubject().getName();
                Double score = sm.getMarksObtained();
                Double maxMarks = (double) sm.getExamSchedule().getMaxMarks();

                double percentage = (score / maxMarks) * 100;

                return SubjectMarkDto.builder()
                                .subject(subjectName)
                                .score(score)
                                .maxMarks(maxMarks)
                                .grade(calculateGrade(percentage))
                                .build();
        }

        private ReportCardDataDto.ResultSummaryDto calculateResultSummary(List<SubjectMarkDto> academics) {
                if (academics.isEmpty()) {
                        return ReportCardDataDto.ResultSummaryDto.builder()
                                        .totalScore(0.0)
                                        .totalMaxScore(0.0)
                                        .percentage(0.0)
                                        .finalGrade("N/A")
                                        .build();
                }

                double totalScore = academics.stream().mapToDouble(SubjectMarkDto::getScore).sum();
                double totalMax = academics.stream().mapToDouble(SubjectMarkDto::getMaxMarks).sum();
                double percentage = totalMax > 0 ? (totalScore / totalMax) * 100 : 0.0;
                percentage = Math.round(percentage * 100.0) / 100.0; // Round to 2 decimals

                return ReportCardDataDto.ResultSummaryDto.builder()
                                .totalScore(totalScore)
                                .totalMaxScore(totalMax)
                                .percentage(percentage)
                                .finalGrade(calculateGrade(percentage))
                                .build();
        }

        private String calculateGrade(double percentage) {
                if (percentage >= 90)
                        return "A+";
                if (percentage >= 80)
                        return "A";
                if (percentage >= 70)
                        return "B+";
                if (percentage >= 60)
                        return "B";
                if (percentage >= 50)
                        return "C";
                if (percentage >= 40)
                        return "D";
                return "F";
        }

        private String generateSession(ExamTerm term) {
                int year = term.getStartDate().getYear();
                return year + "-" + (year + 1);
        }
}
