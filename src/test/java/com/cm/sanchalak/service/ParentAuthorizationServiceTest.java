package com.cm.sanchalak.service;

import com.cm.sanchalak.repository.ParentStudentLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParentAuthorizationServiceTest {
    
    @Mock
    private ParentStudentLinkRepository linkRepository;
    
    @Mock
    private AuditLogService auditLogService;
    
    @InjectMocks
    private ParentAuthorizationService parentAuthorizationService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testIsParentLinkedToStudent_Success() {
        Long parentId = 1L;
        Long studentId = 100L;
        
        when(linkRepository.existsActiveByParentIdAndStudentId(parentId, studentId)).thenReturn(true);
        
        boolean isLinked = parentAuthorizationService.isParentLinkedToStudent(parentId, studentId);
        
        assertTrue(isLinked);
        verify(linkRepository, times(1)).existsActiveByParentIdAndStudentId(parentId, studentId);
    }
    
    @Test
    void testIsParentLinkedToStudent_Failure() {
        Long parentId = 1L;
        Long studentId = 200L;
        
        when(linkRepository.existsActiveByParentIdAndStudentId(parentId, studentId)).thenReturn(false);
        
        boolean isLinked = parentAuthorizationService.isParentLinkedToStudent(parentId, studentId);
        
        assertFalse(isLinked);
        verify(linkRepository, times(1)).existsActiveByParentIdAndStudentId(parentId, studentId);
    }
    
    @Test
    void testValidateParentStudentLinkage_Success() {
        Long parentId = 1L;
        Long studentId = 100L;
        
        // Mock the self-invocation or inject behaviors properly.
        // Wait, @Cacheable won't trigger on internal calls if we call isParentLinkedToStudent here?
        // Actually here we are testing the service method.
        // In unit tests, Spring caching proxy is not active unless we use @SpringBootTest.
        // So we are testing raw logic.
        
        when(linkRepository.existsActiveByParentIdAndStudentId(parentId, studentId)).thenReturn(true);
        
        assertDoesNotThrow(() -> parentAuthorizationService.validateParentStudentLinkage(parentId, studentId));
        
        verify(auditLogService, never()).logAction(any(), any(), any(), any(), any(), any(), any(), any());
    }
    
    @Test
    void testValidateParentStudentLinkage_Unauthorized() {
        Long parentId = 1L;
        Long studentId = 200L;
        
        when(linkRepository.existsActiveByParentIdAndStudentId(parentId, studentId)).thenReturn(false);
        
        SecurityException exception = assertThrows(SecurityException.class, () -> 
            parentAuthorizationService.validateParentStudentLinkage(parentId, studentId));
            
        assertEquals("Parent 1 is not authorized to access student 200 data", exception.getMessage());
        
        verify(auditLogService, times(1)).logAction(
            eq(parentId), 
            eq("UNAUTHORIZED_ACCESS"), 
            eq("STUDENT_DATA"), 
            eq(String.valueOf(studentId)), 
            anyString(), 
            isNull(), 
            isNull(), 
            eq("FAILURE")
        );
    }
}
