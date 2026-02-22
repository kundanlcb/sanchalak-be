package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.AttendanceRecord;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class AttendanceSpecification extends BaseSpecification {
    public static Specification<AttendanceRecord> activeScoped() {
        return BaseSpecification.scoped("schoolClass");
    }

    public static Specification<AttendanceRecord> activeById(Long id) {
        return activeScoped().and((root, query, cb) -> cb.equal(root.get("id"), id));
    }

    public static Specification<AttendanceRecord> byStudentAndDate(Long studentId, LocalDate date) {
        return activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))
                .and((root, query, cb) -> cb.equal(root.get("date"), date));
    }

    public static Specification<AttendanceRecord> byClassAndDate(Long classId, LocalDate date) {
        return activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("schoolClass").get("id"), classId))
                .and((root, query, cb) -> cb.equal(root.get("date"), date));
    }
}
