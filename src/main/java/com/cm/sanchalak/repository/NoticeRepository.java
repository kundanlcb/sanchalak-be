package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {

        @Query("SELECT n FROM Notice n WHERE (n.targetRole IN ('ALL', :targetRole) OR n.createdBy.id = :userId) " +
                        "AND n.publishDate BETWEEN :startDate AND :endDate " +
                        "AND n.isActive = true " +
                        "AND (:schoolId IS NULL OR n.schoolId = :schoolId) " +
                        "ORDER BY n.publishDate DESC, n.priority ASC")
        List<Notice> findByTargetRoleAndPublishDateBetween(
                        @Param("targetRole") String targetRole,
                        @Param("userId") java.util.UUID userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("schoolId") UUID schoolId);

        @Query("SELECT n FROM Notice n WHERE (n.targetRole IN ('ALL', :targetRole) OR n.createdBy.id = :userId) " +
                        "AND n.isActive = true " +
                        "AND (n.expiryDate IS NULL OR n.expiryDate >= :currentDate) " +
                        "AND (:schoolId IS NULL OR n.schoolId = :schoolId) " +
                        "ORDER BY n.priority ASC, n.publishDate DESC")
        List<Notice> findActiveByTargetRole(
                        @Param("targetRole") String targetRole,
                        @Param("userId") java.util.UUID userId,
                        @Param("currentDate") LocalDate currentDate,
                        @Param("schoolId") UUID schoolId);

        @Query("SELECT n FROM Notice n WHERE (n.targetRole IN ('ALL', :targetRole) OR n.createdBy.id = :userId) " +
                        "AND n.isActive = true " +
                        "AND n.publishDate >= :sinceDate " +
                        "AND (:schoolId IS NULL OR n.schoolId = :schoolId) " +
                        "ORDER BY n.publishDate DESC")
        List<Notice> findRecentByTargetRole(
                        @Param("targetRole") String targetRole,
                        @Param("userId") java.util.UUID userId,
                        @Param("sinceDate") LocalDate sinceDate,
                        @Param("schoolId") UUID schoolId);

        @Query("SELECT n FROM Notice n WHERE n.priority = 'HIGH' " +
                        "AND (n.targetRole IN ('ALL', :targetRole) OR n.createdBy.id = :userId) " +
                        "AND n.isActive = true " +
                        "AND (n.expiryDate IS NULL OR n.expiryDate >= :currentDate) " +
                        "AND (:schoolId IS NULL OR n.schoolId = :schoolId) " +
                        "ORDER BY n.publishDate DESC")
        List<Notice> findHighPriorityByTargetRole(
                        @Param("targetRole") String targetRole,
                        @Param("userId") java.util.UUID userId,
                        @Param("currentDate") LocalDate currentDate,
                        @Param("schoolId") UUID schoolId);
}
