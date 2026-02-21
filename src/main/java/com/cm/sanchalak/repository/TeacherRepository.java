package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findByDeletedFalse();
    long countByDeletedFalse();
    Optional<Teacher> findByUserId(UUID userId);
}
