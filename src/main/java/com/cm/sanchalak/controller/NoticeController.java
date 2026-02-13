package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.ApiResult;
import com.cm.sanchalak.dto.NoticeDetailDto;
import com.cm.sanchalak.dto.NoticeDto;
import com.cm.sanchalak.security.CurrentUser;
import com.cm.sanchalak.security.UserPrincipal;
import com.cm.sanchalak.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for notice management
 * Unified API for both web and mobile clients
 */
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {
    
    private final NoticeService noticeService;
    
    /**
     * Get all notices for the authenticated user based on their role
     * Returns notices with read status
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Map<String, Object>> getNotices(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false, defaultValue = "false") boolean onlyRecent,
            @RequestParam(required = false, defaultValue = "false") boolean onlyHighPriority) {
        
        log.info("Fetching notices for user {} (recent: {}, highPriority: {})", 
            currentUser.getId(), onlyRecent, onlyHighPriority);
        
        try {
            // Determine target role based on user's authorities
            String targetRole = determineTargetRole(currentUser);
            
            List<NoticeDto> notices;
            
            if (onlyHighPriority) {
                notices = noticeService.getHighPriorityNotices(currentUser.getId(), targetRole);
            } else if (onlyRecent) {
                notices = noticeService.getRecentNotices(currentUser.getId(), targetRole);
            } else {
                notices = noticeService.getNoticesForUser(currentUser.getId(), targetRole);
            }
            
            long unreadCount = noticeService.getUnreadCount(currentUser.getId(), targetRole);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notices", notices);
            response.put("unreadCount", unreadCount);
            response.put("totalCount", notices.size());
            
            return ApiResult.success(response);
            
        } catch (Exception e) {
            log.error("Error fetching notices for user {}: {}", currentUser.getId(), e.getMessage());
            return ApiResult.error("FETCH_FAILED", "Failed to fetch notices: " + e.getMessage());
        }
    }
    
    /**
     * Get notice details by ID and mark as read
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<NoticeDetailDto> getNoticeDetails(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        
        log.info("Fetching notice {} for user {}", id, currentUser.getId());
        
        try {
            NoticeDetailDto notice = noticeService.getNoticeDetailsAndMarkAsRead(id, currentUser.getId());
            return ApiResult.success(notice);
            
        } catch (RuntimeException e) {
            log.error("Error fetching notice {} for user {}: {}", id, currentUser.getId(), e.getMessage());
            return ApiResult.error("NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching notice {}: {}", id, e.getMessage());
            return ApiResult.error("FETCH_FAILED", "Failed to fetch notice details");
        }
    }
    
    /**
     * Get unread notice count
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Map<String, Long>> getUnreadCount(@CurrentUser UserPrincipal currentUser) {
        
        log.info("Fetching unread notice count for user {}", currentUser.getId());
        
        try {
            String targetRole = determineTargetRole(currentUser);
            long unreadCount = noticeService.getUnreadCount(currentUser.getId(), targetRole);
            
            Map<String, Long> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            
            return ApiResult.success(response);
            
        } catch (Exception e) {
            log.error("Error fetching unread count for user {}: {}", currentUser.getId(), e.getMessage());
            return ApiResult.error("FETCH_FAILED", "Failed to fetch unread count");
        }
    }
    
    /**
     * Determine target role based on user's authorities
     */
    private String determineTargetRole(UserPrincipal userPrincipal) {
        var authorities = userPrincipal.getAuthorities();
        
        if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_PARENT"))) {
            return "PARENT";
        } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"))) {
            return "STUDENT";
        } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"))) {
            return "TEACHER";
        }
        
        // Default to ALL if role cannot be determined
        return "ALL";
    }
}
