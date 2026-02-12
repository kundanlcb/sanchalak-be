package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.cm.sanchalak.dto.ReportCardDto;

@Service
@Transactional
public class AcademicService {

    private final ExamTermRepository examTermRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final StudentMarksRepository studentMarksRepository;
    private final ClassRepository classRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public AcademicService(ExamTermRepository examTermRepository, SubjectRepository subjectRepository,
                           ClassSubjectRepository classSubjectRepository, ExamScheduleRepository examScheduleRepository,
                           StudentMarksRepository studentMarksRepository, ClassRepository classRepository,
                           TeacherRepository teacherRepository, StudentRepository studentRepository) {
        this.examTermRepository = examTermRepository;
        this.subjectRepository = subjectRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.examScheduleRepository = examScheduleRepository;
        this.studentMarksRepository = studentMarksRepository;
        this.classRepository = classRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    public ExamTerm createExamTerm(ExamTerm examTerm) {
        return examTermRepository.save(examTerm);
    }
    
    public List<ExamTerm> getAllTerms() {
        return examTermRepository.findAll();
    }

    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public ClassSubject assignSubjectToClass(Long classId, Long subjectId, Long teacherId) {
        com.cm.sanchalak.entity.Class studentClass = classRepository.findById(classId)
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

    public ExamSchedule scheduleExam(Long termId, Long classId, Long subjectId, java.time.LocalDate date, Integer maxMarks) {
        ExamTerm term = examTermRepository.findById(termId)
                .orElseThrow(() -> new RuntimeException("ExamTerm not found"));
        com.cm.sanchalak.entity.Class studentClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        ExamSchedule schedule = new ExamSchedule();
        schedule.setExamTerm(term);
        schedule.setStudentClass(studentClass);
        schedule.setSubject(subject);
        schedule.setExamDate(date);
        schedule.setMaxMarks(maxMarks);

        return examScheduleRepository.save(schedule);
    }

    public StudentMarks saveStudentMarks(Long scheduleId, Long studentId, Double marksObtained, String remarks) {
        ExamSchedule schedule = examScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Exam Schedule not found"));

        if (marksObtained > schedule.getMaxMarks()) {
            throw new IllegalArgumentException("Marks obtained cannot exceed max marks (" + schedule.getMaxMarks() + ")");
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

    public ReportCardDto generateReportCard(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<StudentMarks> marks = studentMarksRepository.findByStudent(student);

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
}
