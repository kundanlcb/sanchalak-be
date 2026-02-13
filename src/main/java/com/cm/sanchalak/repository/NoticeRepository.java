package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    /**
     * Find notices by target role and publish date range
     */
    @Query("SELECT n FROM Notice n WHERE n.targetRole IN ('ALL', :targetRole) " +
           "AND n.publishDate BETWEEN :startDate AND :endDate " +
           "AND n.isActive = true " +
           "ORDER BY n.publishDate DESC, n.priority ASC")
    List<Notice> findByTargetRoleAndPublishDateBetween(
        @Param("targetRole") String targetRole,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find all active notices for a target role
     */
    @Query("SELECT n FROM Notice n WHERE n.targetRole IN ('ALL', :targetRole) " +
           "AND n.isActive = true " +
           "AND (n.expiryDate IS NULL OR n.expiryDate >= :currentDate) " +
           "ORDER BY n.priority ASC, n.publishDate DESC")
    List<Notice> findActiveByTargetRole(
        @Param("targetRole") String targetRole,
        @Param("currentDate") LocalDate currentDate
    );
    
    /**
     * Find recent notices (last 30 days)
     */
    @Query("SELECT n FROM Notice n WHERE n.targetRole IN ('ALL', :targetRole) " +
           "AND n.isActive = true " +
           "AND n.publishDate >= :sinceDate " +
           "ORDER BY n.publishDate DESC")
    List<Notice> findRecentByTargetRole(
        @Param("targetRole") String targetRole,
        @Param("sinceDate") LocalDate sinceDate
    );
    
    /**
     * Find high priority notices
     */
    @Query("SELECT n FROM Notice n WHERE n.priority = 'HIGH' " +
           "AND n.targetRole IN ('ALL', :targetRole) " +
           "AND n.isActive = true " +
           "AND (n.expiryDate IS NULL OR n.expiryDate >= :currentDate) " +
           "ORDER BY n.publishDate DESC")
    List<Notice> findHighPriorityByTargetRole(
        @Param("targetRole") String targetRole,
        @Param("currentDate") LocalDate currentDate
    );
}
