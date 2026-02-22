package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository
        extends JpaRepository<NotificationLog, Long>, JpaSpecificationExecutor<NotificationLog> {

    @Query("SELECT nl FROM NotificationLog nl WHERE nl.user.id = :userId ORDER BY nl.sentAt DESC")
    Page<NotificationLog> findByUserIdOrderBySentAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationLog nl SET nl.isRead = true, nl.readAt = :readAt WHERE nl.id = :id AND nl.user.id = :userId")
    void markAsRead(@Param("id") Long id, @Param("userId") UUID userId, @Param("readAt") Instant readAt);

    @Query("SELECT nl FROM NotificationLog nl WHERE nl.notificationType = :type ORDER BY nl.sentAt DESC")
    List<NotificationLog> findByNotificationType(@Param("type") String type);

    @Query("SELECT nl FROM NotificationLog nl WHERE nl.deliveryStatus = :status ORDER BY nl.sentAt DESC")
    List<NotificationLog> findByDeliveryStatus(@Param("status") String status);

    @Query("SELECT nl FROM NotificationLog nl WHERE nl.sentAt BETWEEN :startTime AND :endTime ORDER BY nl.sentAt DESC")
    List<NotificationLog> findBySentAtBetween(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    @Query("SELECT COUNT(nl) FROM NotificationLog nl WHERE nl.user.id = :userId AND nl.deliveryStatus = 'DELIVERED'")
    long countDeliveredByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(nl) FROM NotificationLog nl WHERE nl.user.id = :userId AND nl.isRead = false")
    long countUnreadByUserId(@Param("userId") UUID userId);
}
