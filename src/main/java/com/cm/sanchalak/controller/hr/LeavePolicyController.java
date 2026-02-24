package com.cm.sanchalak.controller.hr;

import com.cm.sanchalak.dto.hr.LeaveBalanceDto;
import com.cm.sanchalak.dto.hr.LeaveTypeDto;
import com.cm.sanchalak.service.hr.LeavePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/leave-policies")
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    @GetMapping("/types")
    public ResponseEntity<List<LeaveTypeDto>> getAllLeaveTypes() {
        return ResponseEntity.ok(leavePolicyService.getAllLeaveTypes());
    }

    @PostMapping("/types")
    public ResponseEntity<LeaveTypeDto> createLeaveType(@RequestBody LeaveTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leavePolicyService.createLeaveType(dto));
    }

    @PutMapping("/types/{id}")
    public ResponseEntity<LeaveTypeDto> updateLeaveType(@PathVariable Long id, @RequestBody LeaveTypeDto dto) {
        return ResponseEntity.ok(leavePolicyService.updateLeaveType(id, dto));
    }

    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable Long id) {
        leavePolicyService.deleteLeaveType(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize-balances")
    public ResponseEntity<Void> initializeTeacherBalances(
            @RequestParam(defaultValue = "2024-2025") String academicYear) {
        leavePolicyService.initializeTeacherBalances(academicYear);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balances/{userId}")
    public ResponseEntity<List<LeaveBalanceDto>> getLeaveBalances(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "2024-2025") String academicYear) {
        return ResponseEntity.ok(leavePolicyService.getLeaveBalances(userId, academicYear));
    }
}
