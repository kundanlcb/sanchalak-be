package com.cm.sanchalak.controller.hr;

import com.cm.sanchalak.dto.hr.LeaveActionRequestDto;
import com.cm.sanchalak.dto.hr.LeaveRequestDto;
import com.cm.sanchalak.service.hr.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/leaves")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('TEACHER', 'STAFF', 'SCHOOL_ADMIN')")
    public ResponseEntity<LeaveRequestDto> applyLeave(@RequestBody LeaveRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.applyLeave(dto));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('TEACHER', 'STAFF', 'SCHOOL_ADMIN')")
    public ResponseEntity<List<LeaveRequestDto>> getMyRequests() {
        return ResponseEntity.ok(leaveRequestService.getMyRequests());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<List<LeaveRequestDto>> getPendingRequests() {
        return ResponseEntity.ok(leaveRequestService.getPendingRequests());
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<LeaveRequestDto> processRequest(
            @PathVariable Long id,
            @RequestBody LeaveActionRequestDto action) {
        return ResponseEntity.ok(leaveRequestService.processRequest(id, action));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TEACHER', 'STAFF', 'SCHOOL_ADMIN')")
    public ResponseEntity<Void> cancelRequest(@PathVariable Long id) {
        leaveRequestService.cancelRequest(id);
        return ResponseEntity.noContent().build();
    }
}
