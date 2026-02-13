package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    
    @Query("SELECT nl FROM NotificationLog nl WHERE nl.user.id = :userId ORDER BY nl.sentAt DESC")
    List<NotificationLog> findByUserIdOrderBySentAtDesc(UUID userId);
    
    @Query("SELECT nl FROM NotificationLog nl WHERE nl.notificationType = :type ORDER BY nl.sentAt DESC")
    List<NotificationLog> findByNotificationType(String type);
    
    @Query("SELECT nl FROM NotificationLog nl WHERE nl.deliveryStatus = :status ORDER BY nl.sentAt DESC")
    List<NotificationLog> findByDeliveryStatus(String status);
    
    @Query("SELECT nl FROM NotificationLog nl WHERE nl.sentAt BETWEEN :startTime AND :endTime ORDER BY nl.sentAt DESC")
    List<NotificationLog> findBySentAtBetween(Instant startTime, Instant endTime);
    
    @Query("SELECT COUNT(nl) FROM NotificationLog nl WHERE nl.user.id = :userId AND nl.deliveryStatus = 'DELIVERED'")
    long countDeliveredByUserId(UUID userId);
}
