package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.repository.spec.*;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.exception.AppException;
import com.cm.sanchalak.dto.academic.ReportCardDto;
import com.cm.sanchalak.dto.academic.ExamQuestionDto;
import com.cm.sanchalak.dto.academic.ExamQuestionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AcademicService {

    private final ExamTermRepository examTermRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final StudentMarksRepository studentMarksRepository;
    private final SchoolClassRepository classRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ClassRoutineRepository classRoutineRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final OwnershipValidator ownership;

    public ExamTerm createExamTerm(ExamTerm examTerm) {
        examTerm.setSchoolId(SchoolContext.getSchoolId());
        return examTermRepository.save(examTerm);
    }

    public List<ExamTerm> getAllTerms() {
        return examTermRepository.findAll(ExamTermSpecification.activeScoped());
    }

    public ExamTerm updateExamTerm(Long id, ExamTerm termDetails) {
        ExamTerm term = examTermRepository.findOne(ExamTermSpecification.activeById(id))
                .orElseThrow(() -> new RuntimeException("ExamTerm not found"));

        if (termDetails.getName() != null) {
            term.setName(termDetails.getName());
        }
        if (termDetails.getStartDate() != null) {
            term.setStartDate(termDetails.getStartDate());
        }
        if (termDetails.getEndDate() != null) {
            term.setEndDate(termDetails.getEndDate());
        }

        return examTermRepository.save(term);
    }

    public void deleteExamTerm(Long id) {
        ExamTerm term = examTermRepository.findOne(ExamTermSpecification.activeById(id))
                .orElseThrow(() -> new RuntimeException("ExamTerm not found"));

        // Check if there are any schedules associated with this term to prevent
        // orphaned records or data loss
        if (examScheduleRepository.count(ExamScheduleSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("examTerm").get("id"), id))) > 0) {
            throw new RuntimeException(
                    "Cannot delete an Exam Term that has associated schedules. Please remove the schedules first.");
        }

        examTermRepository.delete(term);
    }

    public Subject createSubject(Subject subject) {
        subject.setSchoolId(SchoolContext.getSchoolId());
        // Prevent duplicate subject code per school+class
        if (subjectRepository.existsByCodeAndSchoolIdAndClassId(
                subject.getCode(), subject.getSchoolId(), subject.getClassId())) {
            throw new RuntimeException("A subject with code '" + subject.getCode()
                    + "' already exists for this class");
        }
        return subjectRepository.save(subject);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll(SubjectSpecification.activeScoped());
    }

    public SchoolClass createClass(SchoolClass schoolClass) {
        if (schoolClass.getClassID() == null || schoolClass.getClassID().isEmpty()) {
            schoolClass.setClassID("CLS-" + System.currentTimeMillis());
        }
        if (schoolClass.getName() == null || schoolClass.getName().isEmpty()) {
            schoolClass.setName("Class " + schoolClass.getGrade() + "-" + schoolClass.getSection());
        }
        schoolClass.setSchoolId(SchoolContext.getSchoolId());
        return classRepository.save(schoolClass);
    }

    public List<SchoolClass> getAllClasses() {
        return classRepository.findAll(SchoolClassSpecification.activeScoped());
    }

    public ClassSubject assignSubjectToClass(Long classId, Long subjectId, Long teacherId) {
        SchoolClass studentClass = classRepository.findOne(SchoolClassSpecification.activeById(classId))
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findOne(SubjectSpecification.activeById(subjectId))
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        Teacher teacher = null;
        if (teacherId != null) {
            teacher = teacherRepository.findOne(TeacherSpecification.activeById(teacherId))
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
        }

        ClassSubject classSubject = new ClassSubject();
        classSubject.setStudentClass(studentClass);
        classSubject.setSubject(subject);
        classSubject.setTeacher(teacher);

        return classSubjectRepository.save(classSubject);
    }

    public ExamSchedule scheduleExam(Long termId, Long classId, Long subjectId, LocalDate date,
            Integer maxMarks, Integer passingMarks, java.time.LocalTime startTime,
            java.time.LocalTime endTime, Integer durationMinutes) {

        ExamSchedule schedule = examScheduleRepository.findOne(ExamScheduleSpecification.byTermAndClass(termId, classId)
                .and((root, query, cb) -> cb.equal(root.get("subject").get("id"), subjectId)))
                .orElseGet(() -> {
                    ExamSchedule newSchedule = new ExamSchedule();
                    ExamTerm term = examTermRepository.findOne(ExamTermSpecification.activeById(termId))
                            .orElseThrow(() -> new RuntimeException("ExamTerm not found"));
                    SchoolClass studentClass = classRepository.findOne(SchoolClassSpecification.activeById(classId))
                            .orElseThrow(() -> new RuntimeException("Class not found"));
                    Subject subject = subjectRepository.findOne(SubjectSpecification.activeById(subjectId))
                            .orElseThrow(() -> new RuntimeException("Subject not found"));

                    newSchedule.setExamTerm(term);
                    newSchedule.setStudentClass(studentClass);
                    newSchedule.setSubject(subject);
                    return newSchedule;
                });

        schedule.setExamDate(date);
        schedule.setMaxMarks(maxMarks);
        schedule.setPassingMarks(passingMarks);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setDurationMinutes(durationMinutes);

        return examScheduleRepository.save(schedule);
    }

    public ExamSchedule updateSchedule(Long scheduleId, LocalDate date, Integer maxMarks,
            Integer passingMarks, java.time.LocalTime startTime, java.time.LocalTime endTime,
            Integer durationMinutes) {
        ExamSchedule schedule = examScheduleRepository.findOne(ExamScheduleSpecification.activeById(scheduleId))
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        if (maxMarks != null)
            schedule.setMaxMarks(maxMarks);
        if (passingMarks != null)
            schedule.setPassingMarks(passingMarks);
        if (startTime != null)
            schedule.setStartTime(startTime);
        if (endTime != null)
            schedule.setEndTime(endTime);
        if (durationMinutes != null)
            schedule.setDurationMinutes(durationMinutes);
        return examScheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long scheduleId) {
        ExamSchedule schedule = examScheduleRepository.findOne(ExamScheduleSpecification.activeById(scheduleId))
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        examScheduleRepository.delete(schedule);
    }

    public List<ExamSchedule> getSchedules(Long termId, Long classId) {
        if (termId != null && classId != null) {
            return examScheduleRepository.findAll(ExamScheduleSpecification.byTermAndClass(termId, classId));
        }
        return examScheduleRepository.findAll(ExamScheduleSpecification.activeScoped());
    }

    public StudentMarks saveStudentMarks(Long scheduleId, Long studentId, Double marksObtained, String remarks) {
        ExamSchedule schedule = examScheduleRepository.findOne(ExamScheduleSpecification.activeById(scheduleId))
                .orElseThrow(() -> new RuntimeException("Exam Schedule not found"));

        if (marksObtained > schedule.getMaxMarks()) {
            throw new IllegalArgumentException(
                    "Marks obtained cannot exceed max marks (" + schedule.getMaxMarks() + ")");
        }

        Student student = studentRepository.findOne(StudentSpecification.activeById(studentId))
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentMarks marks = studentMarksRepository.findOne(StudentMarksSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("examSchedule").get("id"), scheduleId))
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId)))
                .orElse(new StudentMarks());

        if (marks.getId() == null) {
            marks.setExamSchedule(schedule);
            marks.setStudent(student);
        }

        marks.setMarksObtained(marksObtained);
        marks.setRemarks(remarks);

        return studentMarksRepository.save(marks);
    }

    public List<StudentMarks> saveBulkStudentMarks(Long termId, Long classId, Long subjectId,
            List<com.cm.sanchalak.dto.academic.StudentMarkEntryDto> marksList) {
        ExamSchedule schedule = examScheduleRepository.findOne(ExamScheduleSpecification.byTermAndClass(termId, classId)
                .and((root, query, cb) -> cb.equal(root.get("subject").get("id"), subjectId)))
                .orElseThrow(() -> new RuntimeException("Exam Schedule not found for given term, class, and subject"));

        List<StudentMarks> savedMarks = new ArrayList<>();

        for (com.cm.sanchalak.dto.academic.StudentMarkEntryDto entry : marksList) {
            if (entry.getMarksObtained() > schedule.getMaxMarks()) {
                throw new IllegalArgumentException(
                        "Marks obtained cannot exceed max marks (" + schedule.getMaxMarks() + ")");
            }

            Student student = studentRepository.findOne(StudentSpecification.activeById(entry.getStudentId()))
                    .orElseThrow(() -> new RuntimeException("Student not found for ID: " + entry.getStudentId()));

            StudentMarks marks = studentMarksRepository.findOne(StudentMarksSpecification.activeScoped()
                    .and((root, query, cb) -> cb.equal(root.get("examSchedule").get("id"), schedule.getId()))
                    .and((root, query, cb) -> cb.equal(root.get("student").get("id"), student.getId())))
                    .orElse(new StudentMarks());

            if (marks.getId() == null) {
                marks.setExamSchedule(schedule);
                marks.setStudent(student);
            }

            marks.setMarksObtained(entry.getMarksObtained());
            marks.setRemarks(entry.getRemarks());

            savedMarks.add(studentMarksRepository.save(marks));
        }

        return savedMarks;
    }

    public ReportCardDto generateReportCard(Long studentId) {
        Student student = studentRepository.findOne(StudentSpecification.activeById(studentId))
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<StudentMarks> marks = studentMarksRepository.findAll(StudentMarksSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId)));

        return buildReportCard(student, marks);
    }

    public List<StudentMarks> getMarks(Long termId, Long classId, Long subjectId, Long studentId) {
        if (studentId != null) {
            studentRepository.findOne(StudentSpecification.activeById(studentId))
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            return studentMarksRepository.findAll(StudentMarksSpecification.activeScoped()
                    .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                    .and(termId != null
                            ? (root, query, cb) -> cb.equal(root.get("examSchedule").get("examTerm").get("id"), termId)
                            : null)
                    .and(subjectId != null
                            ? (root, query, cb) -> cb.equal(root.get("examSchedule").get("subject").get("id"),
                                    subjectId)
                            : null));
        }

        if (classId != null && subjectId != null && termId != null) {
            return studentMarksRepository.findAll(StudentMarksSpecification.activeScoped()
                    .and((root, query, cb) -> cb.equal(root.get("examSchedule").get("studentClass").get("id"), classId))
                    .and((root, query, cb) -> cb.equal(root.get("examSchedule").get("subject").get("id"), subjectId))
                    .and((root, query, cb) -> cb.equal(root.get("examSchedule").get("examTerm").get("id"), termId)));
        }

        return new ArrayList<>();
    }

    public List<ReportCardDto> getClassTermMarks(Long classId, Long termId) {
        classRepository.findOne(SchoolClassSpecification.activeById(classId))
                .orElseThrow(() -> new RuntimeException("Class not found"));

        examTermRepository.findOne(ExamTermSpecification.activeById(termId))
                .orElseThrow(() -> new RuntimeException("Term not found"));

        List<Student> students = studentRepository.findAll(StudentSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("studentClass").get("id"), classId)));

        List<ReportCardDto> classReports = new ArrayList<>();

        for (Student student : students) {
            List<StudentMarks> termMarks = studentMarksRepository.findAll(StudentMarksSpecification.activeScoped()
                    .and((root, query, cb) -> cb.equal(root.get("student").get("id"), student.getId()))
                    .and((root, query, cb) -> cb.equal(root.get("examSchedule").get("examTerm").get("id"), termId)));

            classReports.add(buildReportCard(student, termMarks));
        }

        return classReports;
    }

    private ReportCardDto buildReportCard(Student student, List<StudentMarks> marks) {
        ReportCardDto report = new ReportCardDto();
        report.setStudentName(student.getName());
        if (student.getStudentClass() != null) {
            report.setClassName(student.getStudentClass().getName());
        }

        Map<String, List<StudentMarks>> marksByTerm = marks.stream()
                .filter(m -> m.getExamSchedule() != null && m.getExamSchedule().getExamTerm() != null)
                .collect(Collectors.groupingBy(m -> m.getExamSchedule().getExamTerm().getName()));

        List<ReportCardDto.TermReport> termReports = new ArrayList<>();

        for (Map.Entry<String, List<StudentMarks>> entry : marksByTerm.entrySet()) {
            ReportCardDto.TermReport termReport = new ReportCardDto.TermReport();
            termReport.setTermName(entry.getKey());

            List<ReportCardDto.SubjectReport> subjectReports = entry.getValue().stream().map(m -> {
                ReportCardDto.SubjectReport sr = new ReportCardDto.SubjectReport();
                sr.setSubjectName(m.getExamSchedule().getSubject().getName());
                sr.setMarksObtained(m.getMarksObtained());
                sr.setMaxMarks(m.getExamSchedule().getMaxMarks());
                return sr;
            }).collect(Collectors.toList());

            termReport.setSubjects(subjectReports);
            termReports.add(termReport);
        }

        report.setTerms(termReports);
        return report;
    }

    public SchoolClass updateClass(Long id, String name) {
        SchoolClass studentClass = classRepository.findOne(SchoolClassSpecification.activeById(id))
                .orElseThrow(() -> new RuntimeException("Class not found"));
        studentClass.setName(name);

        return classRepository.save(studentClass);
    }

    public void deleteClass(Long id) {
        SchoolClass studentClass = classRepository.findOne(SchoolClassSpecification.activeById(id))
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        if (studentRepository.count(StudentSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("studentClass").get("id"), id))) > 0) {
            throw new IllegalArgumentException("Cannot delete class with enrolled students.");
        }

        classRepository.delete(studentClass);
    }

    public Subject updateSubject(Long id, String name, String code) {
        Subject subject = subjectRepository.findOne(SubjectSpecification.activeById(id))
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        subject.setName(name);
        subject.setCode(code);
        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findOne(SubjectSpecification.activeById(id))
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (classSubjectRepository.existsBySubjectId(id)) {
            throw new RuntimeException("Cannot delete subject assigned to classes.");
        }

        if (classRoutineRepository.existsBySubjectId(id)) {
            throw new RuntimeException("Cannot delete subject that is active in the class routine.");
        }

        subjectRepository.delete(subject);
    }

    public ExamQuestionDto addQuestionToExam(Long scheduleId, ExamQuestionRequest request) {
        ExamSchedule schedule = examScheduleRepository.findOne(ExamScheduleSpecification.activeById(scheduleId))
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        Question question = questionRepository.findOne(QuestionSpecification.activeById(request.getQuestionId()))
                .orElseThrow(() -> new RuntimeException("Question not found"));

        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExamSchedule(schedule);
        examQuestion.setQuestion(question);
        examQuestion.setMarks(request.getMarks());
        examQuestion.setSequenceOrder(request.getSequenceOrder());

        examQuestion = examQuestionRepository.save(examQuestion);
        return mapToDto(examQuestion);
    }

    private ExamQuestionDto mapToDto(ExamQuestion eq) {
        ExamQuestionDto dto = new ExamQuestionDto();
        dto.setId(eq.getId());
        dto.setMarks(eq.getMarks());
        dto.setSequenceOrder(eq.getSequenceOrder());

        if (eq.getQuestion() != null) {
            com.cm.sanchalak.dto.curriculum.QuestionDto qDto = com.cm.sanchalak.dto.curriculum.QuestionDto.builder()
                    .id(eq.getQuestion().getId())
                    .questionText(eq.getQuestion().getQuestionText())
                    .questionType(eq.getQuestion().getQuestionType().name())
                    .marks(eq.getQuestion().getMarks())
                    .build();
            dto.setQuestion(qDto);
        }

        return dto;
    }

    public List<ExamQuestionDto> getExamQuestions(Long scheduleId) {
        return examQuestionRepository.findAll(ExamQuestionSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("examSchedule").get("id"), scheduleId)))
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public List<ExamQuestionDto> setExamQuestions(Long scheduleId, List<ExamQuestionRequest> requests) {
        return requests.stream().map(r -> addQuestionToExam(scheduleId, r)).collect(Collectors.toList());
    }

    public void removeQuestionFromExam(Long scheduleId, Long examQuestionId) {
        examQuestionRepository.deleteById(examQuestionId);
    }
}
