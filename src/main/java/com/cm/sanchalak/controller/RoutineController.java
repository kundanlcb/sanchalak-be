package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.academic.RoutineRequest;
import com.cm.sanchalak.dto.academic.RoutineResponse;
import com.cm.sanchalak.service.RoutineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ "/api/academics/routine", "/api/academic/routine" })
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineService routineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<RoutineResponse>> getRoutine(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId) {
        if (classId != null) {
            return ResponseEntity.ok(routineService.getRoutineByClassId(classId));
        } else if (teacherId != null) {
            return ResponseEntity.ok(routineService.getRoutineByTeacherId(teacherId));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoutineResponse> assignSlot(@Valid @RequestBody RoutineRequest request) {
        RoutineResponse routine = routineService.assignSlot(request);
        return ResponseEntity.ok(routine);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> clearSlot(@PathVariable Long id) {
        routineService.clearSlot(id);
        return ResponseEntity.noContent().build();
    }
}
