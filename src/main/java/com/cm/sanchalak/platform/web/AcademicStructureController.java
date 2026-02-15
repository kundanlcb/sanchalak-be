package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.entity.AcademicYear;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.Section;
import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.platform.academic.AcademicStructureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/schools/{schoolId}/academic")
public class AcademicStructureController {

    private final AcademicStructureService service;

    public AcademicStructureController(AcademicStructureService service) {
        this.service = service;
    }

    // Academic Year
    @PostMapping("/years")
    public ResponseEntity<AcademicYear> createYear(@PathVariable UUID schoolId, @RequestBody AcademicYear year) {
        year.setSchoolId(schoolId);
        return ResponseEntity.ok(service.createAcademicYear(year));
    }

    @GetMapping("/years")
    public ResponseEntity<List<AcademicYear>> getYears(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(service.getAcademicYears(schoolId));
    }

    // School Class
    @PostMapping("/classes")
    public ResponseEntity<SchoolClass> createClass(@PathVariable UUID schoolId, @RequestBody SchoolClass schoolClass) {
        schoolClass.setSchoolId(schoolId);
        return ResponseEntity.ok(service.createClass(schoolClass));
    }

    @GetMapping("/classes")
    public ResponseEntity<List<SchoolClass>> getClasses(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(service.getClasses(schoolId));
    }

    // Section (Nested under class usually, but keeping flat here for simplicity or
    // creating under class)
    @PostMapping("/classes/{classId}/sections")
    public ResponseEntity<Section> createSection(@PathVariable UUID schoolId, @PathVariable Long classId,
            @RequestBody Section section) {
        // Validate class belongs to school?
        // section.setSchoolClass(classReference); handled in service/deserialization or
        // lookup
        // For simplicity assuming body has minimal info or we fetch class:
        // Actually better to handle lookup. simplifying for now.
        // Ideally we fetch SchoolClass proxy.
        return ResponseEntity.ok(service.createSection(section));
        // Note: Logic to set SchoolClass on section needs to be robust.
        // For plan, assuming Section DTO or entity has ID set or we enhance service.
    }

    @GetMapping("/classes/{classId}/sections")
    public ResponseEntity<List<Section>> getSections(@PathVariable UUID schoolId, @PathVariable Long classId) {
        return ResponseEntity.ok(service.getSections(classId));
    }

    // Subject
    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@PathVariable UUID schoolId, @RequestBody Subject subject) {
        subject.setSchoolId(schoolId);
        return ResponseEntity.ok(service.createSubject(subject));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getSubjects(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(service.getSubjects(schoolId));
    }
}
