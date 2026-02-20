package com.cm.sanchalak;

import com.cm.sanchalak.dto.analytics.CollectionTrendDto;
import com.cm.sanchalak.dto.analytics.FinancialSummaryDto;
import com.cm.sanchalak.dto.analytics.ReportCardDataDto;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AnalyticsIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private WebTestClient webTestClient;

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ExamTermRepository examTermRepository;
    @Autowired
    private ExamScheduleRepository examScheduleRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private SchoolClassRepository classRepository;
    @Autowired
    private StudentMarksRepository studentMarksRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private FeeStructureRepository feeStructureRepository;
    @Autowired
    private StudentFeeMapRepository studentFeeMapRepository;
    @Autowired
    private FeeCategoryRepository feeCategoryRepository;

    @BeforeEach
    void setUp() {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(this.context).build();

        // Clean up
        studentMarksRepository.deleteAll();
        attendanceRepository.deleteAll();
        examScheduleRepository.deleteAll();
        examTermRepository.deleteAll();
        paymentTransactionRepository.deleteAll();
        studentFeeMapRepository.deleteAll();
        feeStructureRepository.deleteAll(); // items cascade
        studentRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnReportCardData() {
        // Setup Class & Student
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName("Class 10");
        schoolClass = classRepository.save(schoolClass);

        Student student = new Student();
        student.setName("John Doe");
        student.setStudentClass(schoolClass);
        student = studentRepository.save(student);

        // Setup Term
        ExamTerm term = examTermRepository.save(ExamTerm.builder()
                .name("Term 1")
                .startDate(LocalDate.now().minusMonths(3))
                .endDate(LocalDate.now())
                .isActive(true)
                .build());

        // Setup Subjects & Schedule
        Subject math = subjectRepository.save(Subject.builder()
                .name("Math")
                .code("MATH101")
                .build());
        ExamSchedule schedule = new ExamSchedule();
        schedule.setExamTerm(term);
        schedule.setSubject(math);
        schedule.setStudentClass(schoolClass);
        schedule.setExamDate(LocalDate.now().minusMonths(1));
        schedule.setMaxMarks(100);
        schedule = examScheduleRepository.save(schedule);

        // Setup Marks
        StudentMarks marks = new StudentMarks();
        marks.setStudent(student);
        marks.setExamSchedule(schedule);
        marks.setMarksObtained(85.0);
        // maxMarks comes from schedule
        studentMarksRepository.save(marks);

        // Setup Attendance (1 Present out of 1 total in term range)
        AttendanceRecord att = new AttendanceRecord();
        att.setStudent(student);
        att.setSchoolClass(schoolClass);
        att.setDate(LocalDate.now().minusDays(10));
        att.setStatus(AttendanceStatus.PRESENT);
        attendanceRepository.save(att);

        final Long studentId = student.getId();

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/analytics/report-card/{studentId}")
                        .queryParam("termId", term.getId())
                        .build(studentId))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReportCardDataDto.class)
                .value(dto -> {
                    assertThat(dto.getStudent().getName()).contains("John");
                    assertThat(dto.getAcademics()).hasSize(1);
                    assertThat(dto.getAcademics().get(0).getSubject()).isEqualTo("Math");
                    assertThat(dto.getAcademics().get(0).getScore()).isEqualTo(85.0);
                    assertThat(dto.getAttendance().getPresentDays()).isEqualTo(1);
                });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnFinancialSummary() {
        // Setup Fee Structure
        FeeStructure fs = new FeeStructure();
        fs.setName("Annual Fee");
        fs.setAcademicYear("2024-25");
        fs.setFrequency("ANNUAL");

        FeeCategory tuition = feeCategoryRepository.save(FeeCategory.builder()
                .name("Tuition")
                .isMandatory(true)
                .schoolId(UUID.randomUUID()) // Required by entity
                .build());
        FeeStructureItem item = new FeeStructureItem();
        item.setFeeStructure(fs);
        item.setFeeCategory(tuition);
        item.setAmount(new BigDecimal("10000"));
        fs.setItems(List.of(item));
        fs = feeStructureRepository.save(fs);

        // Assign to Student
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName("Class 10B");
        schoolClass = classRepository.save(schoolClass);

        Student student = new Student();
        student.setName("Jane Doe");
        student.setStudentClass(schoolClass);
        student = studentRepository.save(student);

        StudentFeeMap feeMap = new StudentFeeMap();
        feeMap.setStudent(student);
        feeMap.setFeeStructure(fs);
        feeMap.setDiscountAmount(new BigDecimal("1000")); // 10000 - 1000 = 9000 expected
        studentFeeMapRepository.save(feeMap);

        // Make Partial Payment
        PaymentTransaction tx = new PaymentTransaction();
        tx.setStudent(student);
        tx.setAmount(new BigDecimal("4000"));
        tx.setPaymentMethod("CASH");
        tx.setStatus("SUCCESS");
        tx.setPaymentDate(LocalDateTime.now());
        paymentTransactionRepository.save(tx);

        // Act
        webTestClient.get().uri("/api/analytics/finance/summary")
                .exchange()
                .expectStatus().isOk()
                .expectBody(FinancialSummaryDto.class)
                .value(dto -> {
                    assertThat(dto.getTotalExpectedRevenue()).isEqualTo(9000.0);
                    assertThat(dto.getTotalCollected()).isEqualTo(4000.0);
                    assertThat(dto.getTotalOutstanding()).isEqualTo(5000.0);
                });
    }
}
