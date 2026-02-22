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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.cm.sanchalak.security.UserPrincipal;
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
    void shouldReturnReportCardData() {
        // Setup Class & Student
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName("Class 10");
        schoolClass.setSchoolId(UUID.randomUUID());
        schoolClass = classRepository.save(schoolClass);

        Student student = new Student();
        student.setName("John Doe");
        student.setEmail("john.doe@test.com");
        student.setSchoolId(schoolClass.getSchoolId());
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
                .schoolId(schoolClass.getSchoolId())
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

        UserPrincipal principal = new UserPrincipal(
                UUID.randomUUID(),
                "Test Admin",
                "testadmin",
                "admin@test.com",
                "pwd",
                schoolClass.getSchoolId(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/analytics/report-card/{studentId}")
                        .queryParam("termId", term.getId())
                        .build(studentId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.student.name").value(org.hamcrest.Matchers.containsString("John"))
                .jsonPath("$.academics.length()").isEqualTo(1)
                .jsonPath("$.academics[0].subject").isEqualTo("Math")
                .jsonPath("$.academics[0].score").isEqualTo(85.0)
                .jsonPath("$.attendance.presentDays").isEqualTo(1);
    }

    @Test
    void shouldReturnFinancialSummary() {
        // Setup Fee Structure
        FeeStructure fs = new FeeStructure();
        fs.setName("Annual Fee");
        fs.setAcademicYear("2024-25");
        fs.setFrequency("ANNUAL");
        fs.setSchoolId(UUID.randomUUID());

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
        schoolClass.setSchoolId(fs.getSchoolId());
        schoolClass = classRepository.save(schoolClass);

        Student student = new Student();
        student.setName("Jane Doe");
        student.setEmail("jane.doe@test.com");
        student.setSchoolId(schoolClass.getSchoolId());
        student.setStudentClass(schoolClass);
        student = studentRepository.save(student);

        StudentFeeMap feeMap = new StudentFeeMap();
        feeMap.setStudent(student);
        feeMap.setFeeStructure(fs);
        feeMap.setSchoolId(fs.getSchoolId());
        feeMap.setDiscountAmount(new BigDecimal("1000")); // 10000 - 1000 = 9000 expected
        studentFeeMapRepository.save(feeMap);

        // Make Partial Payment
        PaymentTransaction tx = new PaymentTransaction();
        tx.setStudent(student);
        tx.setSchoolId(fs.getSchoolId());
        tx.setAmount(new BigDecimal("4000"));
        tx.setPaymentMethod("CASH");
        tx.setStatus("SUCCESS");
        tx.setPaymentDate(LocalDateTime.now());
        paymentTransactionRepository.save(tx);

        UserPrincipal principal = new UserPrincipal(
                UUID.randomUUID(),
                "Test Admin",
                "testadmin",
                "admin@test.com",
                "pwd",
                fs.getSchoolId(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        // Act
        webTestClient.get().uri("/api/analytics/finance/summary")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalExpectedRevenue").isEqualTo(9000.0)
                .jsonPath("$.totalCollected").isEqualTo(4000.0)
                .jsonPath("$.totalOutstanding").isEqualTo(5000.0);
    }
}
