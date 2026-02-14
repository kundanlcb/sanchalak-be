package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.NoticeDetailDto;
import com.cm.sanchalak.dto.NoticeDto;
import com.cm.sanchalak.entity.Notice;
import com.cm.sanchalak.entity.NoticeReadStatus;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.NoticeReadStatusRepository;
import com.cm.sanchalak.repository.NoticeRepository;
import com.cm.sanchalak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing notices and read status
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {
    
    private final NoticeRepository noticeRepository;
    private final NoticeReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all active notices for a user based on their role
     */
    @Transactional(readOnly = true)
    public List<NoticeDto> getNoticesForUser(UUID userId, String targetRole) {
        log.info("Fetching notices for user {} with role {}", userId, targetRole);
        
        LocalDate currentDate = LocalDate.now();
        List<Notice> notices = noticeRepository.findActiveByTargetRole(targetRole, currentDate);
        
        return notices.stream()
            .map(notice -> convertToDto(notice, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Get recent notices (last 30 days)
     */
    @Transactional(readOnly = true)
    public List<NoticeDto> getRecentNotices(UUID userId, String targetRole) {
        log.info("Fetching recent notices for user {} with role {}", userId, targetRole);
        
        LocalDate sinceDate = LocalDate.now().minusDays(30);
        List<Notice> notices = noticeRepository.findRecentByTargetRole(targetRole, sinceDate);
        
        return notices.stream()
            .map(notice -> convertToDto(notice, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Get high priority notices
     */
    @Transactional(readOnly = true)
    public List<NoticeDto> getHighPriorityNotices(UUID userId, String targetRole) {
        log.info("Fetching high priority notices for user {} with role {}", userId, targetRole);
        
        LocalDate currentDate = LocalDate.now();
        List<Notice> notices = noticeRepository.findHighPriorityByTargetRole(targetRole, currentDate);
        
        return notices.stream()
            .map(notice -> convertToDto(notice, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Get notice details and mark as read
     */
    @Transactional
    public NoticeDetailDto getNoticeDetailsAndMarkAsRead(Long noticeId, UUID userId) {
        log.info("Fetching notice {} for user {}", noticeId, userId);
        
        Notice notice = noticeRepository.findById(noticeId)
            .orElseThrow(() -> new RuntimeException("Notice not found"));
        
        // Mark as read if not already read
        NoticeReadStatus readStatus = readStatusRepository
            .findByUserIdAndNoticeId(userId, noticeId)
            .orElse(null);
        
        if (readStatus == null) {
            readStatus = new NoticeReadStatus();
            readStatus.setUserId(userId);
            readStatus.setNotice(notice);
            readStatus.setReadAt(Instant.now());
            readStatusRepository.save(readStatus);
            log.info("Marked notice {} as read for user {}", noticeId, userId);
        }
        
        return convertToDetailDto(notice, readStatus);
    }
    
    /**
     * Get unread notice count for a user
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId, String targetRole) {
        return readStatusRepository.countUnreadByUserIdAndTargetRole(userId, targetRole);
    }
    
    /**
     * Convert Notice entity to NoticeDto
     */
    private NoticeDto convertToDto(Notice notice, UUID userId) {
        boolean isRead = readStatusRepository.existsByUserIdAndNoticeId(userId, notice.getId());
        
        String createdByName = null;
        if (notice.getCreatedBy() != null) {
            createdByName = notice.getCreatedBy().getName();
        }
        
        return new NoticeDto(
            notice.getId(),
            notice.getTitle(),
            notice.getPriority(),
            notice.getTargetRole(),
            notice.getPublishDate(),
            notice.getExpiryDate(),
            isRead,
            notice.getAttachmentUrl(),
            createdByName
        );
    }
    
    /**
     * Convert Notice entity to NoticeDetailDto
     */
    private NoticeDetailDto convertToDetailDto(Notice notice, NoticeReadStatus readStatus) {
        String createdByName = null;
        if (notice.getCreatedBy() != null) {
            createdByName = notice.getCreatedBy().getName();
        }
        
        return new NoticeDetailDto(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getPriority(),
            notice.getTargetRole(),
            notice.getPublishDate(),
            notice.getExpiryDate(),
            readStatus != null,
            notice.getAttachmentUrl(),
            createdByName,
            readStatus != null ? readStatus.getReadAt() : null
        );
    }
    /**
     * Create a new notice
     */
    @Transactional
    public NoticeDto createNotice(com.cm.sanchalak.dto.NoticeRequest request, UUID userId) {
        log.info("Creating notice for user {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPriority(request.getPriority());
        notice.setTargetRole(request.getTargetRole());
        notice.setPublishDate(request.getPublishDate() != null ? request.getPublishDate() : LocalDate.now());
        notice.setExpiryDate(request.getExpiryDate());
        notice.setAttachmentUrl(request.getAttachmentUrl());
        notice.setCreatedBy(user);
        notice.setIsActive(true);
        
        notice = noticeRepository.save(notice);
        log.info("Created notice with ID: {}", notice.getId());
        
        return convertToDto(notice, userId);
    }

    /**
     * Update an existing notice
     */
    @Transactional
    public NoticeDto updateNotice(Long id, com.cm.sanchalak.dto.NoticeRequest request, UUID userId) {
        log.info("Updating notice {} for user {}", id, userId);
        
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notice not found"));
            
        // TODO: Add permission check if needed (e.g. only creator or admin can update)
        
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPriority(request.getPriority());
        notice.setTargetRole(request.getTargetRole());
        if (request.getPublishDate() != null) {
            notice.setPublishDate(request.getPublishDate());
        }
        notice.setExpiryDate(request.getExpiryDate());
        notice.setAttachmentUrl(request.getAttachmentUrl());
        
        notice = noticeRepository.save(notice);
        log.info("Updated notice with ID: {}", notice.getId());
        
        return convertToDto(notice, userId);
    }

    /**
     * Delete a notice (soft delete)
     */
    @Transactional
    public void deleteNotice(Long id, UUID userId) {
        log.info("Deleting notice {} for user {}", id, userId);
        
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notice not found"));
            
        // Soft delete
        notice.setIsActive(false);
        noticeRepository.save(notice);
        
        log.info("Deleted (soft) notice with ID: {}", id);
    }
}
