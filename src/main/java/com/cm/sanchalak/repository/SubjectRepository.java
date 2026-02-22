package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {
    List<Subject> findBySchoolId(UUID schoolId);

    boolean existsByCodeAndSchoolIdAndClassId(String code, UUID schoolId, Long classId);
}
