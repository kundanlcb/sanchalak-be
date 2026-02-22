package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long>, JpaSpecificationExecutor<Teacher> {
    List<Teacher> findByDeletedFalse();

    List<Teacher> findBySchoolIdAndDeletedFalse(UUID schoolId);

    long countByDeletedFalse();

    long countBySchoolIdAndDeletedFalse(UUID schoolId);

    Optional<Teacher> findByUserId(UUID userId);
}
