package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.cm.sanchalak.dto.academic.ReportCardDto;
import com.cm.sanchalak.dto.academic.ExamQuestionDto;
import com.cm.sanchalak.dto.academic.ExamQuestionRequest;
import com.cm.sanchalak.dto.curriculum.QuestionDto;

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

    public ExamTerm createExamTerm(ExamTerm examTerm) {
        return examTermRepository.save(examTerm);
    }

    public List<ExamTerm> getAllTerms() {
        return examTermRepository.findAll();
    }

    public ExamTerm updateExamTerm(Long id, ExamTerm termDetails) {
        ExamTerm term = examTermRepository.findById(id)
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

    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public SchoolClass createClass(SchoolClass schoolClass) {
        if (schoolClass.getClassID() == null || schoolClass.getClassID().isEmpty()) {
            schoolClass.setClassID("CLS-" + System.currentTimeMillis());
        }
        if (schoolClass.getName() == null || schoolClass.getName().isEmpty()) {
            schoolClass.setName("Class " + schoolClass.getGrade() + "-" + schoolClass.getSection());
        }
        return classRepository.save(schoolClass);
    }

    public List<SchoolClass> getAllClasses() {
        return classRepository.findAll();
    }

    public ClassSubject assignSubjectToClass(Long classId, Long subjectId, Long teacherId) {
        SchoolClass studentClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        Teacher teacher = null;
        if (teacherId != null) {
            teacher = teacherRepository.findById(teacherId)
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

        ExamSchedule schedule = examScheduleRepository
                .findByExamTerm_IdAndStudentClass_IdAndSubject_Id(termId, classId, subjectId)
                .orElseGet(() -> {
                    ExamSchedule newSchedule = new ExamSchedule();
                    ExamTerm term = examTermRepository.findById(termId)
                            .orElseThrow(() -> new RuntimeException("ExamTerm not found"));
                    SchoolClass studentClass = classRepository.findById(classId)
                            .orElseThrow(() -> new RuntimeException("Class not found"));
                    Subject subject = subjectRepository.findById(subjectId)
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
        ExamSchedule schedule = examScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        if (date != null)
            schedule.setExamDate(date);
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
        examScheduleRepository.deleteById(scheduleId);
    }

    public List<ExamSchedule> getSchedules(Long termId, Long classId) {
        if (termId != null && classId != null) {
            return examScheduleRepository.findByExamTerm_IdAndStudentClass_Id(termId, classId);
        }
        return examScheduleRepository.findAll();
    }

    public StudentMarks saveStudentMarks(Long scheduleId, Long studentId, Double marksObtained, String remarks) {
        ExamSchedule schedule = examScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Exam Schedule not found"));

        if (marksObtained > schedule.getMaxMarks()) {
            throw new IllegalArgumentException(
                    "Marks obtained cannot exceed max marks (" + schedule.getMaxMarks() + ")");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentMarks marks = studentMarksRepository.findByExamScheduleAndStudent(schedule, student)
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
        ExamSchedule schedule = examScheduleRepository
                .findByExamTerm_IdAndStudentClass_IdAndSubject_Id(termId, classId, subjectId)
                .orElseThrow(() -> new RuntimeException("Exam Schedule not found for given term, class, and subject"));

        List<StudentMarks> savedMarks = new ArrayList<>();

        for (com.cm.sanchalak.dto.academic.StudentMarkEntryDto entry : marksList) {
            if (entry.getMarksObtained() > schedule.getMaxMarks()) {
                throw new IllegalArgumentException(
                        "Marks obtained cannot exceed max marks (" + schedule.getMaxMarks() + ")");
            }

            Student student = studentRepository.findById(entry.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found for ID: " + entry.getStudentId()));

            StudentMarks marks = studentMarksRepository.findByExamScheduleAndStudent(schedule, student)
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
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<StudentMarks> marks = studentMarksRepository.findByStudent(student);
        return buildReportCard(student, marks);
    }

    public List<StudentMarks> getMarks(Long termId, Long classId, Long subjectId, Long studentId) {
        if (studentId != null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            List<StudentMarks> marks = studentMarksRepository.findByStudent(student);
            if (termId != null) {
                marks = marks.stream()
                        .filter(m -> m.getExamSchedule().getExamTerm().getId().equals(termId))
                        .collect(Collectors.toList());
            }
            if (subjectId != null) {
                marks = marks.stream()
                        .filter(m -> m.getExamSchedule().getSubject().getId().equals(subjectId))
                        .collect(Collectors.toList());
            }
            return marks;
        }

        if (classId != null && subjectId != null && termId != null) {
            return studentMarksRepository
                    .findByExamSchedule_StudentClass_IdAndExamSchedule_Subject_IdAndExamSchedule_ExamTerm_Id(classId,
                            subjectId, termId);
        }

        return new ArrayList<>();
    }

    public List<ReportCardDto> getClassTermMarks(Long classId, Long termId) {
        // Use findByStudentClass_Id which exists in repository
        List<Student> students = studentRepository.findByStudentClass_Id(classId);

        ExamTerm term = examTermRepository.findById(termId)
                .orElseThrow(() -> new RuntimeException("Term not found"));

        List<ReportCardDto> classReports = new ArrayList<>();

        for (Student student : students) {
            List<StudentMarks> marks = studentMarksRepository.findByStudent(student);
            // Filter by term
            List<StudentMarks> termMarks = marks.stream()
                    .filter(m -> m.getExamSchedule().getExamTerm().getId().equals(termId))
                    .collect(Collectors.toList());

            if (!termMarks.isEmpty()) {
                classReports.add(buildReportCard(student, termMarks));
            } else {
                // Include student even if no marks? Yes, empty report.
                classReports.add(buildReportCard(student, new ArrayList<>()));
            }
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

    // Class Management

    public SchoolClass updateClass(Long id, String name) {
        SchoolClass studentClass = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        studentClass.setName(name);
        return classRepository.save(studentClass);
    }

    public void deleteClass(Long id) {
        if (!classRepository.existsById(id)) {
            throw new IllegalArgumentException("Class not found");
        }
        // Check dependencies
        if (studentRepository.countByStudentClassId(id) > 0) {
            throw new IllegalArgumentException("Cannot delete class with enrolled students.");
        }
        // In a real implementation we would also check for class routines, subjects
        // etc.
        // For now, blocking if students exist is the primary safety check.

        classRepository.deleteById(id);
    }

    // Subject Management

    public Subject updateSubject(Long id, String name, String code) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        subject.setName(name);
        subject.setCode(code);
        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new RuntimeException("Subject not found");
        }
        // Check if subject is associated with any class
        if (classSubjectRepository.existsBySubjectId(id)) {
            throw new RuntimeException("Cannot delete subject assigned to classes.");
        }

        // Check if subject is active in routine
        if (classRoutineRepository.existsBySubjectId(id)) {
            throw new RuntimeException("Cannot delete subject that is active in the class routine.");
        }

        subjectRepository.deleteById(id);
    }

    // --- Exam Questions ---

    public ExamQuestionDto addQuestionToExam(Long scheduleId, ExamQuestionRequest request) {
        ExamSchedule schedule = examScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        ExamQuestion eq = new ExamQuestion();
        eq.setExamSchedule(schedule);
        eq.setQuestion(question);
        eq.setMarks(request.getMarks());
        eq.setSequenceOrder(request.getSequenceOrder());

        return mapToExamQuestionDto(examQuestionRepository.save(eq));
    }

    @Transactional(readOnly = true)
    public List<ExamQuestionDto> getExamQuestions(Long scheduleId) {
        return examQuestionRepository.findByExamSchedule_IdOrderBySequenceOrderAsc(scheduleId)
                .stream().map(this::mapToExamQuestionDto).collect(Collectors.toList());
    }

    public void removeQuestionFromExam(Long scheduleId, Long examQuestionId) {
        examQuestionRepository.deleteById(examQuestionId);
    }

    public List<ExamQuestionDto> setExamQuestions(Long scheduleId, List<ExamQuestionRequest> requests) {
        ExamSchedule schedule = examScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        examQuestionRepository.deleteByExamSchedule_Id(scheduleId);

        List<ExamQuestion> questions = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            ExamQuestionRequest req = requests.get(i);
            Question question = questionRepository.findById(req.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found: " + req.getQuestionId()));
            ExamQuestion eq = new ExamQuestion();
            eq.setExamSchedule(schedule);
            eq.setQuestion(question);
            eq.setMarks(req.getMarks());
            eq.setSequenceOrder(req.getSequenceOrder() != null ? req.getSequenceOrder() : i + 1);
            questions.add(eq);
        }

        return examQuestionRepository.saveAll(questions)
                .stream().map(this::mapToExamQuestionDto).collect(Collectors.toList());
    }

    private ExamQuestionDto mapToExamQuestionDto(ExamQuestion eq) {
        Question q = eq.getQuestion();
        List<QuestionDto.QuestionOptionDto> optionDtos = new ArrayList<>();
        if (q.getOptions() != null) {
            optionDtos = q.getOptions().stream().map(o -> QuestionDto.QuestionOptionDto.builder()
                    .id(o.getId())
                    .optionText(o.getOptionText())
                    .isCorrect(o.getIsCorrect())
                    .build()).collect(Collectors.toList());
        }

        QuestionDto questionDto = QuestionDto.builder()
                .id(q.getId())
                .chapterId(q.getChapter().getId())
                .questionText(q.getQuestionText())
                .questionType(q.getQuestionType().name())
                .marks(q.getMarks())
                .options(optionDtos)
                .build();

        return ExamQuestionDto.builder()
                .id(eq.getId())
                .examScheduleId(eq.getExamSchedule().getId())
                .marks(eq.getMarks())
                .sequenceOrder(eq.getSequenceOrder())
                .question(questionDto)
                .build();
    }
}
