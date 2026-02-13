package com.cm.sanchalak.controller;

import com.cm.sanchalak.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/gender-distribution")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getGenderDistribution() {
        return ResponseEntity.ok(dashboardService.getGenderDistribution());
    }

    @GetMapping("/teacher-performance")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getTeacherPerformance() {
        return ResponseEntity.ok(dashboardService.getTeacherPerformance());
    }

    @GetMapping("/activity-feed")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> getActivityFeed() {
        return ResponseEntity.ok(dashboardService.getActivityFeed());
    }
}
