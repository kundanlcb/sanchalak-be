package com.cm.sanchalak.service;

import com.cm.sanchalak.repository.ParentStudentLinkRepository;
import com.cm.sanchalak.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for validating parent-student linkage with caching
 * Cache TTL: 1 hour (configured in CacheConfig)
 */
@Service
@RequiredArgsConstructor
public class ParentAuthorizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ParentAuthorizationService.class);
    
    private final ParentStudentLinkRepository linkRepository;
    private final AuditLogService auditLogService;
    
    /**
     * Validate if parent has access to student data
     * Result is cached for 1 hour to improve performance
     * 
     * @param parentId Parent ID
     * @param studentId Student ID
     * @return true if active linkage exists, false otherwise
     */
    @Cacheable(cacheNames = "parent-linkage", key = "#parentId + '_' + #studentId")
    @Transactional(readOnly = true)
    public boolean isParentLinkedToStudent(Long parentId, Long studentId) {
        boolean isLinked = linkRepository.existsActiveByParentIdAndStudentId(parentId, studentId);
        
        if (!isLinked) {
            logger.warn("Unauthorized parent access attempt: parent={}, student={}", parentId, studentId);
        }
        
        return isLinked;
    }
    
    /**
     * Validate linkage and throw exception if not authorized
     */
    @Transactional(readOnly = true)
    public void validateParentStudentLinkage(Long parentId, Long studentId) {
        if (!isParentLinkedToStudent(parentId, studentId)) {
            auditLogService.logAction(
                parentId, 
                "UNAUTHORIZED_ACCESS", 
                "STUDENT_DATA", 
                String.valueOf(studentId), 
                "Parent attempted unauthorized access to student data", 
                null, 
                null, 
                "FAILURE"
            );
            
            throw new SecurityException(
                String.format("Parent %d is not authorized to access student %d data", parentId, studentId)
            );
        }
    }
}
