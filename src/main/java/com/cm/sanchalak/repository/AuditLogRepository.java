package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AuditLog> findByActionTypeOrderByCreatedAtDesc(String actionType);

    List<AuditLog> findByCreatedAtAfter(LocalDateTime timestamp);

    List<AuditLog> findTop10ByOrderByCreatedAtDesc();
}
