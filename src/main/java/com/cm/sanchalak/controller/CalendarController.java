package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.ApiResult;
import com.cm.sanchalak.dto.CalendarEventDto;
import com.cm.sanchalak.entity.Parent;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.repository.ParentRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.security.CurrentUser;
import com.cm.sanchalak.security.UserPrincipal;
import com.cm.sanchalak.service.CalendarAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for aggregated calendar events
 * Unified API for both web and mobile clients
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
public class CalendarController {
    
    private final CalendarAggregationService calendarService;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    
    /**
     * Get aggregated calendar events for the authenticated user
     * For STUDENT role: returns student's own events
     * For PARENT role: returns events for all linked children
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<CalendarEventDto>> getCalendarEvents(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching calendar events for user {} from {} to {}", 
            currentUser.getId(), startDate, endDate);
        
        try {
            // Default to current month if no date range provided
            if (startDate == null) {
                startDate = LocalDate.now().withDayOfMonth(1);
            }
            if (endDate == null) {
                endDate = startDate.plusMonths(1).minusDays(1);
            }
            
            // Determine if user is STUDENT or PARENT
            var authorities = currentUser.getAuthorities();
            List<CalendarEventDto> events;
            
            if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"))) {
                // Find student by userId
                Student student = studentRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Student profile not found for user"));
                
                events = calendarService.getCalendarEventsForStudent(student.getId(), startDate, endDate);
                
            } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_PARENT"))) {
                // Find parent by userId
                Parent parent = parentRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Parent profile not found for user"));
                
                events = calendarService.getCalendarEventsForParent(parent.getId(), startDate, endDate);
                
            } else {
                // For other roles (TEACHER, ADMIN), return empty list or handle differently
                log.warn("Calendar endpoint accessed by non-student/parent user: {}", currentUser.getId());
                return ApiResult.error("UNAUTHORIZED", "Calendar is only accessible to students and parents");
            }
            
            log.info("Returning {} calendar events for user {}", events.size(), currentUser.getId());
            return ApiResult.success(events);
            
        } catch (RuntimeException e) {
            log.error("Error fetching calendar events for user {}: {}", currentUser.getId(), e.getMessage());
            return ApiResult.error("NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching calendar events: {}", e.getMessage());
            return ApiResult.error("FETCH_FAILED", "Failed to fetch calendar events");
        }
    }
    
    /**
     * Get calendar events for a specific student (for parent viewing child's calendar)
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResult<List<CalendarEventDto>> getCalendarEventsForStudent(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Parent {} fetching calendar events for student {} from {} to {}", 
            currentUser.getId(), studentId, startDate, endDate);
        
        try {
            // Default to current month if no date range provided
            if (startDate == null) {
                startDate = LocalDate.now().withDayOfMonth(1);
            }
            if (endDate == null) {
                endDate = startDate.plusMonths(1).minusDays(1);
            }
            
            // TODO: Add parent-student linkage validation here
            // For now, assume validation is handled at service layer
            
            List<CalendarEventDto> events = calendarService.getCalendarEventsForStudent(
                studentId, startDate, endDate
            );
            
            log.info("Returning {} calendar events for student {}", events.size(), studentId);
            return ApiResult.success(events);
            
        } catch (Exception e) {
            log.error("Error fetching calendar events for student {}: {}", studentId, e.getMessage());
            return ApiResult.error("FETCH_FAILED", "Failed to fetch calendar events");
        }
    }
}
