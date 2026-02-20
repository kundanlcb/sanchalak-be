package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.HomeworkSubmissionDto;
import com.cm.sanchalak.dto.PresignedUrlDto;
import com.cm.sanchalak.entity.Homework;
import com.cm.sanchalak.entity.HomeworkSubmission;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.repository.HomeworkRepository;
import com.cm.sanchalak.repository.HomeworkSubmissionRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HomeworkSubmissionServiceTest {

    @Mock
    private HomeworkSubmissionRepository submissionRepository;

    @Mock
    private HomeworkRepository homeworkRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private HomeworkSubmissionService submissionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateUploadUrl_Success() {
        Long homeworkId = 1L;
        Long studentId = 100L;
        String fileName = "assignment.pdf";
        String contentType = "application/pdf";

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(new Homework()));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student()));
        when(fileStorageService.generateUploadUrl(anyString(), anyString(), anyInt()))
                .thenReturn("https://s3.aws.com/upload-url");

        PresignedUrlDto result = submissionService.generateUploadUrl(homeworkId, studentId, fileName, contentType);

        assertNotNull(result);
        assertEquals("https://s3.aws.com/upload-url", result.getUploadUrl());
        assertNotNull(result.getObjectKey());
        assertTrue(result.getObjectKey().endsWith(".pdf"));
    }

    @Test
    void testSubmitHomework_Success_Late() {
        Long homeworkId = 1L;
        Long studentId = 100L;

        Homework homework = new Homework();
        homework.setDueDate(LocalDate.now().minusDays(1)); // Due yesterday

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student()));
        when(submissionRepository.findByHomeworkIdAndStudentId(homeworkId, studentId)).thenReturn(Optional.empty());
        when(submissionRepository.save(any(HomeworkSubmission.class))).thenAnswer(i -> i.getArguments()[0]);

        HomeworkSubmissionDto result = submissionService.submitHomework(homeworkId, studentId,
                Collections.singletonList("http://file.url"), "Here is my work");

        assertNotNull(result);
        assertEquals("SUBMITTED", result.getStatus());
        assertTrue(result.getIsLate());
    }

    @Test
    void testSubmitHomework_Success_OnTime() {
        Long homeworkId = 1L;
        Long studentId = 100L;

        Homework homework = new Homework();
        homework.setDueDate(LocalDate.now().plusDays(1)); // Due tomorrow

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student()));
        when(submissionRepository.findByHomeworkIdAndStudentId(homeworkId, studentId)).thenReturn(Optional.empty());
        when(submissionRepository.save(any(HomeworkSubmission.class))).thenAnswer(i -> i.getArguments()[0]);

        HomeworkSubmissionDto result = submissionService.submitHomework(homeworkId, studentId,
                Collections.singletonList("http://file.url"), "Here is my work");

        assertNotNull(result);
        assertEquals("SUBMITTED", result.getStatus());
        assertFalse(result.getIsLate());
    }
}
