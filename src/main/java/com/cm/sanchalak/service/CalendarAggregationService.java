package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.CalendarEventDto;
import com.cm.sanchalak.entity.ExamSchedule;
import com.cm.sanchalak.entity.Notice;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.repository.ExamScheduleRepository;
import com.cm.sanchalak.repository.NoticeRepository;
import com.cm.sanchalak.repository.ParentStudentLinkRepository;
import com.cm.sanchalak.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for aggregating calendar events from multiple sources
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarAggregationService {
    
    private final ExamScheduleRepository examScheduleRepository;
    private final NoticeRepository noticeRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    
    /**
     * Get aggregated calendar events for a student
     */
    @Transactional(readOnly = true)
    public List<CalendarEventDto> getCalendarEventsForStudent(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching calendar events for student {} from {} to {}", studentId, startDate, endDate);
        
        List<CalendarEventDto> events = new ArrayList<>();
        
        // Get student class
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        
        if (student.getStudentClass() != null) {
            // Add exam events
            events.addAll(getExamEvents(student.getStudentClass().getId(), startDate, endDate));
        }
        
        // Add notice events
        events.addAll(getNoticeEvents("STUDENT", startDate, endDate));
        
        // Sort by date
        events.sort(Comparator.comparing(CalendarEventDto::getEventDate));
        
        return events;
    }
    
    /**
     * Get aggregated calendar events for a parent (includes all linked children)
     */
    @Transactional(readOnly = true)
    public List<CalendarEventDto> getCalendarEventsForParent(Long parentId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching calendar events for parent {} from {} to {}", parentId, startDate, endDate);
        
        List<CalendarEventDto> events = new ArrayList<>();
        Set<Long> studentClassIds = new HashSet<>();
        
        // Get all linked students
        var links = parentStudentLinkRepository.findByParentIdAndIsActiveTrue(parentId);
        
        for (var link : links) {
            Student student = link.getStudent();
            if (student != null && student.getStudentClass() != null) {
                studentClassIds.add(student.getStudentClass().getId());
            }
        }
        
        // Add exam events for all children's classes
        for (Long classId : studentClassIds) {
            events.addAll(getExamEvents(classId, startDate, endDate));
        }
        
        // Add notice events for parents
        events.addAll(getNoticeEvents("PARENT", startDate, endDate));
        
        // Remove duplicates (same exam in multiple children's classes)
        events = events.stream()
            .distinct()
            .sorted(Comparator.comparing(CalendarEventDto::getEventDate))
            .collect(Collectors.toList());
        
        return events;
    }
    
    /**
     * Get exam schedule events for a specific class
     */
    private List<CalendarEventDto> getExamEvents(Long classId, LocalDate startDate, LocalDate endDate) {
        List<ExamSchedule> exams = examScheduleRepository.findByStudentClassIdAndExamDateBetween(
            classId, startDate, endDate
        );
        
        return exams.stream()
            .filter(exam -> exam.getExamDate() != null)
            .map(exam -> {
                String title = String.format("%s - %s", 
                    exam.getSubject().getName(),
                    exam.getExamTerm().getName()
                );
                
                String description = String.format("Max Marks: %d", exam.getMaxMarks());
                
                return new CalendarEventDto(
                    "EXAM",
                    exam.getId(),
                    title,
                    description,
                    exam.getExamDate(),
                    null,
                    exam.getSubject().getName(),
                    exam.getStudentClass().getName(),
                    buildExamMetadata(exam)
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get notice events
     */
    private List<CalendarEventDto> getNoticeEvents(String targetRole, LocalDate startDate, LocalDate endDate) {
        List<Notice> notices = noticeRepository.findByTargetRoleAndPublishDateBetween(
            targetRole, startDate, endDate
        );
        
        return notices.stream()
            .map(notice -> new CalendarEventDto(
                "NOTICE",
                notice.getId(),
                notice.getTitle(),
                null, // Don't include full content in calendar view
                notice.getPublishDate(),
                notice.getPriority(),
                null,
                null,
                null
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Build metadata JSON for exam events
     */
    private String buildExamMetadata(ExamSchedule exam) {
        return String.format(
            "{\"examTermId\":%d,\"subjectId\":%d,\"classId\":%d,\"maxMarks\":%d}",
            exam.getExamTerm().getId(),
            exam.getSubject().getId(),
            exam.getStudentClass().getId(),
            exam.getMaxMarks()
        );
    }
}
