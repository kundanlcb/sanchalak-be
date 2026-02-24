package com.cm.sanchalak.repository.spec;

import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.StudentStatus;
import org.springframework.data.jpa.domain.Specification;

public class StudentSpecification extends BaseSpecification {

    public static Specification<Student> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Student> hasId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    /**
     * Combines school-based filtering and soft-delete filtering.
     */
    public static Specification<Student> activeScoped() {
        return Specification.where(isNotDeleted()).and(scoped());
    }

    public static Specification<Student> hasByClassId(Long classId) {
        return (root, query, cb) -> cb.equal(root.get("studentClass").get("id"), classId);
    }

    public static Specification<Student> hasStatus(StudentStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Student> activeById(Long id) {
        return activeScoped().and(hasId(id));
    }
}
