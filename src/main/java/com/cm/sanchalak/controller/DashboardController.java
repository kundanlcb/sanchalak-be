package com.cm.sanchalak.controller;

import com.cm.sanchalak.security.CurrentUser;
import com.cm.sanchalak.security.UserPrincipal;
import com.cm.sanchalak.service.DashboardAggregationService;
import com.cm.sanchalak.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SCHOOL_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/gender-distribution")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<?> getGenderDistribution() {
        return ResponseEntity.ok(dashboardService.getGenderDistribution());
    }

    @GetMapping("/teacher-performance")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<?> getTeacherPerformance() {
        return ResponseEntity.ok(dashboardService.getTeacherPerformance());
    }

    @GetMapping("/activity-feed")
    @PreAuthorize("hasRole('SCHOOL_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getActivityFeed() {
        return ResponseEntity.ok(dashboardService.getActivityFeed());
    }

    private final DashboardAggregationService aggregationService;

    @GetMapping("/overview/student")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<?> getStudentDashboard(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(aggregationService.getDashboardForStudentByUser(currentUser.getId()));
    }

    @GetMapping("/overview/teacher")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getTeacherDashboard(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(aggregationService.getDashboardForTeacher(currentUser.getId()));
    }

    @GetMapping("/overview/parent")
    @PreAuthorize("hasRole('ROLE_PARENT')")
    public ResponseEntity<?> getParentDashboard(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(aggregationService.getDashboardForParentByUser(currentUser.getId()));
    }
}
