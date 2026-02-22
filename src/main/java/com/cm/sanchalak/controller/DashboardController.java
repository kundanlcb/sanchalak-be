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
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/gender-distribution")
    public ResponseEntity<?> getGenderDistribution() {
        return ResponseEntity.ok(dashboardService.getGenderDistribution());
    }

    @GetMapping("/teacher-performance")
    public ResponseEntity<?> getTeacherPerformance() {
        return ResponseEntity.ok(dashboardService.getTeacherPerformance());
    }

    @GetMapping("/activity-feed")
    public ResponseEntity<?> getActivityFeed() {
        return ResponseEntity.ok(dashboardService.getActivityFeed());
    }

    private final DashboardAggregationService aggregationService;

    @GetMapping("/overview/student")
    public ResponseEntity<?> getStudentDashboard(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(aggregationService.getDashboardForStudentByUser(currentUser.getId()));
    }

    @GetMapping("/overview/teacher")
    public ResponseEntity<?> getTeacherDashboard(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(aggregationService.getDashboardForTeacher(currentUser.getId()));
    }

    @GetMapping("/overview/parent")
    public ResponseEntity<?> getParentDashboard(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(aggregationService.getDashboardForParentByUser(currentUser.getId()));
    }
}
