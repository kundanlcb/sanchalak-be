package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTokenRepository
        extends JpaRepository<NotificationToken, Long>, JpaSpecificationExecutor<NotificationToken> {

    @Query("SELECT nt FROM NotificationToken nt WHERE nt.user.id = :userId AND nt.isActive = true")
    List<NotificationToken> findActiveByUserId(UUID userId);

    Optional<NotificationToken> findByTokenValue(String tokenValue);

    @Query("SELECT nt FROM NotificationToken nt WHERE nt.user.id = :userId AND nt.tokenValue = :tokenValue")
    Optional<NotificationToken> findByUserIdAndTokenValue(UUID userId, String tokenValue);

    @Query("SELECT DISTINCT nt.user.id FROM NotificationToken nt WHERE nt.isActive = true")
    List<UUID> findAllActiveUserIds();

    @Query("SELECT nt FROM NotificationToken nt WHERE nt.deviceId = :deviceId AND nt.isActive = true")
    List<NotificationToken> findActiveByDeviceId(String deviceId);
}
