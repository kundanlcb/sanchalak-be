package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.NoticeReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoticeReadStatusRepository extends JpaRepository<NoticeReadStatus, Long> {
    
    /**
     * Check if a user has read a specific notice
     */
    @Query("SELECT CASE WHEN COUNT(nrs) > 0 THEN true ELSE false END " +
           "FROM NoticeReadStatus nrs " +
           "WHERE nrs.userId = :userId AND nrs.notice.id = :noticeId")
    boolean existsByUserIdAndNoticeId(
        @Param("userId") UUID userId,
        @Param("noticeId") Long noticeId
    );
    
    /**
     * Find read status for a user and notice
     */
    Optional<NoticeReadStatus> findByUserIdAndNoticeId(UUID userId, Long noticeId);
    
    /**
     * Find all notices read by a user
     */
    List<NoticeReadStatus> findByUserId(UUID userId);
    
    /**
     * Count unread notices for a user
     */
    @Query("SELECT COUNT(n) FROM Notice n " +
           "WHERE n.targetRole IN ('ALL', :targetRole) " +
           "AND n.isActive = true " +
           "AND NOT EXISTS (SELECT 1 FROM NoticeReadStatus nrs " +
           "WHERE nrs.notice.id = n.id AND nrs.userId = :userId)")
    long countUnreadByUserIdAndTargetRole(
        @Param("userId") UUID userId,
        @Param("targetRole") String targetRole
    );
}
