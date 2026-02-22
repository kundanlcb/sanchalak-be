package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.*;
import com.cm.sanchalak.dto.academic.ReportCardDto;
import com.cm.sanchalak.dto.academic.RoutineResponse;
import com.cm.sanchalak.dto.finance.StudentLedgerDto;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.HomeworkRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.security.CurrentUser;
import com.cm.sanchalak.security.UserPrincipal;
import com.cm.sanchalak.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Unified profile and dashboard controller for both mobile and web
 * Works with any JWT authentication (web login or mobile OTP)
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final DashboardAggregationService dashboardService;
    private final ParentService parentService;
    private final TeacherRepository teacherRepository;
    private final ParentAuthorizationService parentAuthorizationService;
    private final AttendanceService attendanceService;
    private final RoutineService routineService;
    private final FinanceService financeService;
    private final AcademicService academicService;
    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionService homeworkSubmissionService;

    /**
     * Get current user profile with auto-resolved student/parent info
     * GET /api/me
     * Works for: Mobile app, Web app
     */
    @GetMapping
    public ResponseEntity<ApiResult<UserProfileDto>> getCurrentUser(@CurrentUser UserPrincipal currentUser) {

        logger.info("Fetching profile for user: {}", currentUser.getId());

        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        User user = userOpt.get();

        // Build base profile
        String fullName = user.getName();
        String firstName = fullName;
        String lastName = "";

        if (fullName != null && fullName.contains(" ")) {
            int lastSpaceIndex = fullName.lastIndexOf(" ");
            firstName = fullName.substring(0, lastSpaceIndex);
            lastName = fullName.substring(lastSpaceIndex + 1);
        }

        UserProfileDto.UserProfileDtoBuilder profileBuilder = UserProfileDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .name(fullName)
                .firstName(firstName)
                .lastName(lastName)
                .role(user.getRoles().iterator().next().getName().name())
                .schoolId(currentUser.getSchoolId());

        // Auto-resolve student info if ROLE_STUDENT
        if (hasRole(user, RoleName.ROLE_STUDENT)) {
            Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                profileBuilder
                        .studentId(student.getId())
                        .className(student.getStudentClass() != null ? student.getStudentClass().getName() : null)
                        .rollNo(student.getRollNo());
            }
        }

        // Auto-resolve parent info if ROLE_PARENT
        if (hasRole(user, RoleName.ROLE_PARENT)) {
            Optional<Parent> parentOpt = parentService.getParentByUserId(user.getId());
            if (parentOpt.isPresent()) {
                profileBuilder.parentId(parentOpt.get().getId());
                profileBuilder.parentID(parentOpt.get().getParentID());
            }
        }

        // Auto-resolve teacher info if ROLE_TEACHER
        if (hasRole(user, RoleName.ROLE_TEACHER)) {
            Optional<Teacher> teacherOpt = teacherRepository.findByUserId(user.getId());
            if (teacherOpt.isPresent()) {
                Teacher teacher = teacherOpt.get();
                profileBuilder
                        .teacherId(teacher.getId())
                        .qualification(teacher.getQualification())
                        .specializations(teacher.getSpecializations().stream()
                                .map(Subject::getName)
                                .collect(Collectors.toList()));
            }
        }

        UserProfileDto profile = profileBuilder.build();

        return ResponseEntity.ok(ApiResult.success(profile));
    }

    /**
     * Get personalized dashboard for current user
     * GET /api/me/home
     * GET /api/me/home?studentId={id} (for parents viewing specific child)
     * Works for: Mobile app, Web app
     * Returns different data based on user role (Student, Parent, Teacher, Admin)
     */
    @GetMapping("/home")
    public ResponseEntity<ApiResult<DashboardDto>> getDashboard(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) Long studentId) {

        logger.info("Fetching dashboard for user: {}, studentId: {}", currentUser.getId(), studentId);

        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        User user = userOpt.get();

        DashboardDto dashboard;

        if (hasRole(user, RoleName.ROLE_STUDENT)) {
            // Get student dashboard
            Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
            if (studentOpt.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResult.error("STUDENT_NOT_FOUND", "Student profile not found for this user"));
            }

            dashboard = dashboardService.getDashboardForStudent(studentOpt.get().getId());

        } else if (hasRole(user, RoleName.ROLE_PARENT)) {
            // Get parent dashboard
            Optional<Parent> parentOpt = parentService.getParentByUserId(user.getId());
            if (parentOpt.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResult.error("PARENT_NOT_FOUND", "Parent profile not found for this user"));
            }

            Long parentId = parentOpt.get().getId();

            if (studentId != null) {
                // Parent viewing specific child's dashboard - validate linkage
                try {
                    parentAuthorizationService.validateParentStudentLinkage(parentId, studentId);
                    dashboard = dashboardService.getDashboardForStudent(studentId);
                } catch (SecurityException e) {
                    logger.warn("Unauthorized parent access: parent={}, student={}", parentId, studentId);
                    return ResponseEntity
                            .status(403)
                            .body(ApiResult.error("PARENT_NOT_AUTHORIZED",
                                    "You are not authorized to access this student's data"));
                }
            } else {
                // Parent viewing aggregated dashboard of all children
                dashboard = dashboardService.getDashboardForParent(user.getId());
            }

        } else if (hasRole(user, RoleName.ROLE_TEACHER)) {
            // Get teacher dashboard
            dashboard = dashboardService.getDashboardForTeacher(user.getId());

        } else if (hasRole(user, RoleName.ROLE_SCHOOL_ADMIN)) {
            // Get admin dashboard
            dashboard = dashboardService.getDashboardForParent(user.getId()); // Use shared notice logic for now

        } else {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResult.error("UNSUPPORTED_ROLE", "Dashboard not available for this role"));
        }

        return ResponseEntity.ok(ApiResult.success(dashboard));
    }

    /**
     * Get all students linked to current parent
     * GET /api/me/students
     * Only accessible to ROLE_PARENT users
     * Works for: Mobile app, Web app
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResult<List<LinkedStudentDto>>> getLinkedStudents(
            @CurrentUser UserPrincipal currentUser) {

        logger.info("Fetching linked students for parent user: {}", currentUser.getId());

        try {
            List<LinkedStudentDto> students = parentService.getLinkedStudentsByUserId(currentUser.getId());
            return ResponseEntity.ok(ApiResult.success(students));

        } catch (IllegalArgumentException e) {
            logger.error("Parent profile not found for user: {}", currentUser.getId());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResult.error("PARENT_NOT_FOUND", "Parent profile not found for this user"));
        }
    }

    /**
     * Get attendance summary for current user
     * GET /api/me/attendance/summary
     * GET /api/me/attendance/summary?studentId={id} (for parents)
     * Auto-resolves studentId for STUDENT role, validates linkage for PARENT role
     */
    @GetMapping("/attendance/summary")
    public ResponseEntity<ApiResult<AttendanceSummaryDto>> getAttendanceSummary(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) Long studentId) {

        logger.info("Fetching attendance summary for user: {}, param studentId: {}", currentUser.getId(), studentId);

        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        Long resolvedStudentId;

        if (hasRole(user, RoleName.ROLE_STUDENT)) {
            // Auto-resolve studentId for STUDENT
            Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
            if (studentOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResult.error("STUDENT_NOT_FOUND", "Student profile not found"));
            }
            resolvedStudentId = studentOpt.get().getId();

        } else if (hasRole(user, RoleName.ROLE_PARENT)) {
            // Validate parent-student linkage
            if (studentId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResult.error("STUDENT_ID_REQUIRED", "studentId parameter required for parents"));
            }

            Optional<Parent> parentOpt = parentService.getParentByUserId(user.getId());
            if (parentOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResult.error("PARENT_NOT_FOUND", "Parent profile not found"));
            }

            try {
                parentAuthorizationService.validateParentStudentLinkage(parentOpt.get().getId(), studentId);
                resolvedStudentId = studentId;
            } catch (SecurityException e) {
                return ResponseEntity.status(403)
                        .body(ApiResult.error("AUTHZ_001", "You are not authorized to access this student's data"));
            }

        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("UNSUPPORTED_ROLE", "This endpoint is for students and parents only"));
        }

        AttendanceSummaryDto summary = attendanceService.getStudentAttendanceSummary(resolvedStudentId,
                LocalDate.now().withDayOfMonth(1), LocalDate.now());
        return ResponseEntity.ok(ApiResult.success(summary));
    }

    /**
     * Get attendance history for current user
     * GET /api/me/attendance/history
     * GET /api/me/attendance/history?studentId={id}&startDate={date}&endDate={date}
     * (for parents)
     * Auto-resolves studentId for STUDENT role, validates linkage for PARENT role
     */
    @GetMapping("/attendance/history")
    public ResponseEntity<ApiResult<List<AttendanceRecordDto>>> getAttendanceHistory(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.info("Fetching attendance history for user: {}", currentUser.getId());

        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        if (resolvedStudentId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_RESOLUTION_FAILED", "Unable to resolve student"));
        }

        List<AttendanceRecordDto> attendance = attendanceService
                .getStudentAttendanceHistory(resolvedStudentId, startDate, endDate);
        return ResponseEntity.ok(ApiResult.success(attendance));
    }

    /**
     * Get weekly timetable for current user
     * GET /api/me/timetable
     * GET /api/me/timetable?studentId={id} (for parents)
     * Auto-resolves studentId and classId for STUDENT role, validates linkage for
     * PARENT role
     */
    @GetMapping("/timetable")
    public ResponseEntity<ApiResult<TimetableDto>> getTimetable(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) Long studentId) {

        logger.info("Fetching timetable for user: {}", currentUser.getId());

        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();

        if (hasRole(user, RoleName.ROLE_TEACHER)) {
            Optional<Teacher> teacherOpt = teacherRepository.findByUserId(user.getId());
            if (teacherOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResult.error("TEACHER_NOT_FOUND", "Teacher profile not found"));
            }

            List<RoutineResponse> routineList = routineService.getRoutineByTeacherId(teacherOpt.get().getId());

            // Transform RoutineResponse to TimetableDto format
            Map<String, List<TimetableDto.PeriodDto>> weeklySchedule = routineList.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getDayOfWeek().name(),
                            Collectors.mapping(
                                    r -> TimetableDto.PeriodDto.builder()
                                            .periodNumber(r.getPeriod())
                                            .startTime(r.getStartTime() != null ? r.getStartTime().toString() : null)
                                            .endTime(r.getEndTime() != null ? r.getEndTime().toString() : null)
                                            .subjectName(r.getSubjectName())
                                            .teacherName(r.getTeacherName())
                                            .className(r.getClassName()) // We might need className in DTO for teachers
                                                                         // to know which class
                                            .periodType("LECTURE")
                                            .build(),
                                    Collectors.toList())));

            TimetableDto timetable = TimetableDto.builder()
                    .studentId(null)
                    .className("Teacher Schedule")
                    .weeklySchedule(weeklySchedule)
                    .build();

            return ResponseEntity.ok(ApiResult.success(timetable));
        }

        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        if (resolvedStudentId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_RESOLUTION_FAILED", "Unable to resolve student"));
        }

        // Get student to retrieve classId
        Optional<Student> studentOpt = studentRepository.findById(resolvedStudentId);
        if (studentOpt.isEmpty() || studentOpt.get().getStudentClass() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("CLASS_NOT_FOUND", "Student class information not found"));
        }

        Long classId = studentOpt.get().getStudentClass().getId();
        List<RoutineResponse> routineList = routineService.getRoutineByClassId(classId);

        // Transform RoutineResponse to TimetableDto format
        Map<String, List<TimetableDto.PeriodDto>> weeklySchedule = routineList.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getDayOfWeek().name(),
                        Collectors.mapping(
                                r -> TimetableDto.PeriodDto.builder()
                                        .periodNumber(r.getPeriod())
                                        .startTime(r.getStartTime() != null ? r.getStartTime().toString() : null)
                                        .endTime(r.getEndTime() != null ? r.getEndTime().toString() : null)
                                        .subjectName(r.getSubjectName())
                                        .teacherName(r.getTeacherName())
                                        .periodType("LECTURE") // Default type
                                        .build(),
                                Collectors.toList())));

        TimetableDto timetable = TimetableDto.builder()
                .studentId(resolvedStudentId)
                .className(studentOpt.get().getStudentClass().getName())
                .weeklySchedule(weeklySchedule)
                .build();

        return ResponseEntity.ok(ApiResult.success(timetable));
    }

    /**
     * Get academic results for current user
     * GET /api/me/results
     * GET /api/me/results?studentId={id} (for parents)
     * Auto-resolves studentId for STUDENT role, validates linkage for PARENT role
     */
    @GetMapping("/results")
    public ResponseEntity<ApiResult<ResultsDto>> getResults(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) Long studentId) {

        logger.info("Fetching results for user: {}", currentUser.getId());

        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        if (resolvedStudentId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_RESOLUTION_FAILED", "Unable to resolve student"));
        }

        Optional<Student> studentOpt = studentRepository.findById(resolvedStudentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_NOT_FOUND", "Student not found"));
        }

        Student student = studentOpt.get();

        // Fetch report card from AcademicService
        ReportCardDto reportCard = academicService.generateReportCard(resolvedStudentId);

        // Transform ReportCardDto to ResultsDto
        List<ResultsDto.ExamResultDto> examResults = reportCard.getTerms().stream()
                .map(term -> {
                    double totalMax = term.getSubjects().stream()
                            .mapToDouble(s -> s.getMaxMarks() != null ? s.getMaxMarks().doubleValue() : 0.0)
                            .sum();
                    double totalObtained = term.getSubjects().stream()
                            .mapToDouble(s -> s.getMarksObtained() != null ? s.getMarksObtained() : 0.0)
                            .sum();
                    double percentage = totalMax > 0 ? (totalObtained / totalMax) * 100 : 0;

                    List<ResultsDto.SubjectScoreDto> subjectScores = term.getSubjects().stream()
                            .map(subject -> ResultsDto.SubjectScoreDto.builder()
                                    .subjectName(subject.getSubjectName())
                                    .maxMarks(subject.getMaxMarks() != null ? subject.getMaxMarks().doubleValue() : 0.0)
                                    .obtainedMarks(
                                            subject.getMarksObtained() != null ? subject.getMarksObtained() : 0.0)
                                    .build())
                            .collect(Collectors.toList());

                    return ResultsDto.ExamResultDto.builder()
                            .examName(term.getTermName())
                            .subjectScores(subjectScores)
                            .totalMarks(totalMax)
                            .obtainedMarks(totalObtained)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());

        ResultsDto results = ResultsDto.builder()
                .studentId(resolvedStudentId)
                .studentName(student.getName())
                .className(student.getStudentClass() != null ? student.getStudentClass().getName() : null)
                .examResults(examResults)
                .build();

        return ResponseEntity.ok(ApiResult.success(results));
    }

    /**
     * Get homework list for current user
     * GET /api/me/homework
     * GET /api/me/homework?studentId={id} (for parents)
     * Auto-resolves studentId for STUDENT role, validates linkage for PARENT role
     */
    @GetMapping("/homework")
    public ResponseEntity<ApiResult<HomeworkListDto>> getHomework(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) Long studentId) {

        logger.info("Fetching homework for user: {}", currentUser.getId());

        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        if (resolvedStudentId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_RESOLUTION_FAILED", "Unable to resolve student"));
        }

        // Get student's class
        Optional<Student> studentOpt = studentRepository.findById(resolvedStudentId);
        if (studentOpt.isEmpty() || studentOpt.get().getStudentClass() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("CLASS_NOT_FOUND", "Student class information not found"));
        }

        Long classId = studentOpt.get().getStudentClass().getId();
        List<Homework> allHomework = homeworkRepository.findByStudentClassId(classId);

        LocalDate today = LocalDate.now();

        // Transform homework to HomeworkItemDto and categorize by status
        List<HomeworkListDto.HomeworkItemDto> pending = new ArrayList<>();
        List<HomeworkListDto.HomeworkItemDto> completed = new ArrayList<>();

        for (Homework hw : allHomework) {
            long daysUntilDue = ChronoUnit.DAYS.between(today, hw.getDueDate());
            String status = daysUntilDue < 0 ? "OVERDUE" : "PENDING";

            HomeworkListDto.HomeworkItemDto item = HomeworkListDto.HomeworkItemDto.builder()
                    .homeworkId(hw.getId())
                    .title(hw.getTitle())
                    .description(hw.getDescription())
                    .subjectName(hw.getSubject() != null ? hw.getSubject().getName() : null)
                    .teacherName(hw.getTeacher() != null ? hw.getTeacher().getName() : null)
                    .dueDate(hw.getDueDate())
                    .status(status)
                    .daysUntilDue((int) daysUntilDue)
                    .build();

            // Categorize (Note: actual submission tracking will come in Phase 6)
            if ("OVERDUE".equals(status) || "PENDING".equals(status)) {
                pending.add(item);
            }
        }

        HomeworkListDto homework = HomeworkListDto.builder()
                .pending(pending)
                .completed(completed)
                .build();

        return ResponseEntity.ok(ApiResult.success(homework));
    }

    /**
     * Get fee ledger for current user
     * GET /api/me/fees/ledger
     * GET /api/me/fees/ledger?studentId={id} (for parents)
     * Auto-resolves studentId for STUDENT role, validates linkage for PARENT role
     */
    @GetMapping("/fees/ledger")
    public ResponseEntity<ApiResult<StudentLedgerDto>> getFeeLedger(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) Long studentId) {

        logger.info("Fetching fee ledger for user: {}", currentUser.getId());

        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        if (resolvedStudentId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_RESOLUTION_FAILED", "Unable to resolve student"));
        }

        try {
            StudentLedgerDto ledger = financeService.getStudentLedger(resolvedStudentId);
            return ResponseEntity.ok(ApiResult.success(ledger));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Helper method to resolve studentId with proper authorization
     * Returns null if resolution fails
     */
    private Long resolveStudentIdWithAuthorization(UserPrincipal currentUser, Long studentId) {
        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        if (hasRole(user, RoleName.ROLE_STUDENT)) {
            // Auto-resolve for STUDENT
            Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
            return studentOpt.map(Student::getId).orElse(null);

        } else if (hasRole(user, RoleName.ROLE_PARENT)) {
            // Validate linkage for PARENT
            if (studentId == null) {
                return null;
            }

            Optional<Parent> parentOpt = parentService.getParentByUserId(user.getId());
            if (parentOpt.isEmpty()) {
                return null;
            }

            try {
                parentAuthorizationService.validateParentStudentLinkage(parentOpt.get().getId(), studentId);
                return studentId;
            } catch (SecurityException e) {
                logger.warn("Parent authorization failed: {}", e.getMessage());
                return null;
            }
        }

        return null;
    }

    /**
     * Generate presigned upload URL for homework submission
     * POST /api/me/homework/{homeworkId}/upload-url
     * STUDENT role only
     */
    @PostMapping("/homework/{homeworkId}/upload-url")
    public ResponseEntity<ApiResult<PresignedUrlDto>> generateHomeworkUploadUrl(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long homeworkId,
            @RequestParam String fileName,
            @RequestParam String contentType) {

        logger.info("Generating upload URL for homework: {}, user: {}", homeworkId, currentUser.getId());

        // Auto-resolve studentId for STUDENT role
        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("USER_NOT_FOUND", "User not found"));
        }

        User user = userOpt.get();
        if (!hasRole(user, RoleName.ROLE_STUDENT)) {
            return ResponseEntity.status(403)
                    .body(ApiResult.error("STUDENT_ONLY", "Only students can upload homework"));
        }

        Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_NOT_FOUND", "Student profile not found"));
        }

        try {
            PresignedUrlDto urlDto = homeworkSubmissionService.generateUploadUrl(
                    homeworkId, studentOpt.get().getId(), fileName, contentType);
            return ResponseEntity.ok(ApiResult.success(urlDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("VALIDATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Submit homework after files are uploaded
     * POST /api/me/homework/{homeworkId}/submit
     * STUDENT role only
     */
    @PostMapping("/homework/{homeworkId}/submit")
    public ResponseEntity<ApiResult<HomeworkSubmissionDto>> submitHomework(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long homeworkId,
            @RequestBody SubmitHomeworkRequest request) {

        logger.info("Submitting homework: {}, user: {}", homeworkId, currentUser.getId());

        // Auto-resolve studentId for STUDENT role
        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("USER_NOT_FOUND", "User not found"));
        }

        User user = userOpt.get();
        Optional<Student> studentOpt = studentRepository.findByUserId(user.getId());
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_NOT_FOUND", "Student profile not found"));
        }

        try {
            HomeworkSubmissionDto submission = homeworkSubmissionService.submitHomework(
                    homeworkId,
                    studentOpt.get().getId(),
                    request.getFileUrls(),
                    request.getRemarks());
            return ResponseEntity.ok(ApiResult.success(submission));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("SUBMISSION_ERROR", e.getMessage()));
        }
    }

    /**
     * Get homework submission details
     * GET /api/me/homework/{homeworkId}/submission
     * GET /api/me/homework/{homeworkId}/submission?studentId={id} (for parents)
     * Auto-resolves studentId for STUDENT role, validates linkage for PARENT role
     */
    @GetMapping("/homework/{homeworkId}/submission")
    public ResponseEntity<ApiResult<HomeworkSubmissionDto>> getHomeworkSubmission(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long homeworkId,
            @RequestParam(required = false) Long studentId) {

        logger.info("Fetching homework submission: homework={}, user={}", homeworkId, currentUser.getId());

        Long resolvedStudentId = resolveStudentIdWithAuthorization(currentUser, studentId);
        if (resolvedStudentId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResult.error("STUDENT_RESOLUTION_FAILED", "Unable to resolve student"));
        }

        Optional<HomeworkSubmissionDto> submissionOpt = homeworkSubmissionService.getSubmission(
                homeworkId, resolvedStudentId);

        if (submissionOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResult.error("SUBMISSION_NOT_FOUND", "No submission found for this homework"));
        }

        return ResponseEntity.ok(ApiResult.success(submissionOpt.get()));
    }

    /**
     * Request DTO for homework submission
     */
    @Data
    public static class SubmitHomeworkRequest {
        private List<String> fileUrls;
        private String remarks;
    }

    /**
     * Check if user has specific role
     */
    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }
}
