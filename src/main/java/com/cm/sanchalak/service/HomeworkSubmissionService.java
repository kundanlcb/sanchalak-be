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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for homework submission management
 * Handles file upload validation, submission creation, and late status marking
 */
@Service
@Transactional
@RequiredArgsConstructor
public class HomeworkSubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(HomeworkSubmissionService.class);

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "application/pdf");
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "pdf");

    private final HomeworkSubmissionRepository submissionRepository;
    private final HomeworkRepository homeworkRepository;
    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;

    /**
     * Generate presigned upload URL for homework submission file
     */
    public PresignedUrlDto generateUploadUrl(Long homeworkId, Long studentId, String fileName, String contentType) {
        // Validate file
        validateFileType(contentType, fileName);

        // Verify homework and student exist
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new IllegalArgumentException("Homework not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // Generate unique object key
        String extension = getFileExtension(fileName);
        String objectKey = String.format("homework/%d/student_%d/%s.%s",
                homeworkId, studentId, UUID.randomUUID().toString(), extension);

        // Generate presigned URL (15 minutes expiry)
        String uploadUrl = fileStorageService.generateUploadUrl(objectKey, contentType, 15);

        logger.info("Generated upload URL for homework: {} ({}), student: {} ({})",
                homeworkId, homework.getTitle(), studentId, student.getName());

        return PresignedUrlDto.builder()
                .uploadUrl(uploadUrl)
                .objectKey(objectKey)
                .expiryMinutes(15)
                .instructions("Upload your file to this URL using PUT request with Content-Type header")
                .build();
    }

    /**
     * Submit homework with file URLs
     */
    public HomeworkSubmissionDto submitHomework(Long homeworkId, Long studentId, List<String> fileUrls,
            String remarks) {
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new IllegalArgumentException("Homework not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // Check if already submitted
        Optional<HomeworkSubmission> existingOpt = submissionRepository.findByHomeworkIdAndStudentId(homeworkId,
                studentId);

        HomeworkSubmission submission;
        if (existingOpt.isPresent()) {
            // Resubmission
            submission = existingOpt.get();
            submission.setStatus(HomeworkSubmission.SubmissionStatus.RESUBMITTED);
            submission.setSubmissionFileUrls(fileUrls);
            submission.setStudentRemarks(remarks);
            submission.setSubmittedAt(Instant.now());

            logger.info("Homework resubmitted: homework={}, student={}", homeworkId, studentId);
        } else {
            // New submission
            submission = new HomeworkSubmission();
            submission.setHomework(homework);
            submission.setStudent(student);
            submission.setSubmissionFileUrls(fileUrls);
            submission.setStudentRemarks(remarks);
            submission.setSubmittedAt(Instant.now());
            submission.setStatus(HomeworkSubmission.SubmissionStatus.SUBMITTED);

            logger.info("Homework submitted: homework={}, student={}", homeworkId, studentId);
        }

        // Check if late
        boolean isLate = checkIfLate(homework.getDueDate(), submission.getSubmittedAt());
        submission.setIsLate(isLate);

        submission = submissionRepository.save(submission);

        return mapToDto(submission);
    }

    /**
     * Get submission by homework and student
     */
    @Transactional(readOnly = true)
    public Optional<HomeworkSubmissionDto> getSubmission(Long homeworkId, Long studentId) {
        return submissionRepository.findByHomeworkIdAndStudentId(homeworkId, studentId)
                .map(this::mapToDto);
    }

    /**
     * Get all submissions for a students
     */
    @Transactional(readOnly = true)
    public List<HomeworkSubmissionDto> getStudentSubmissions(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Validate file type and size
     */
    private void validateFileType(String contentType, String fileName) {
        // Check content type
        if (!ALLOWED_FILE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("File type not allowed. Allowed types: %s", String.join(", ", ALLOWED_FILE_TYPES)));
        }

        // Check file extension
        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    String.format("File extension not allowed. Allowed extensions: %s",
                            String.join(", ", ALLOWED_EXTENSIONS)));
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            throw new IllegalArgumentException("File name must have an extension");
        }
        return fileName.substring(lastDot + 1);
    }

    /**
     * Check if submission is late
     */
    private boolean checkIfLate(LocalDate dueDate, Instant submittedAt) {
        LocalDate submittedDate = submittedAt.atZone(ZoneId.systemDefault()).toLocalDate();
        return submittedDate.isAfter(dueDate);
    }

    /**
     * Map entity to DTO
     */
    private HomeworkSubmissionDto mapToDto(HomeworkSubmission submission) {
        return HomeworkSubmissionDto.builder()
                .submissionId(submission.getId())
                .homeworkId(submission.getHomework().getId())
                .homeworkTitle(submission.getHomework().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getName())
                .submittedAt(submission.getSubmittedAt())
                .isLate(submission.getIsLate())
                .status(submission.getStatus().name())
                .fileUrls(submission.getSubmissionFileUrls())
                .studentRemarks(submission.getStudentRemarks())
                .teacherFeedback(submission.getTeacherFeedback())
                .grade(submission.getGrade())
                .marksObtained(submission.getMarksObtained())
                .gradedAt(submission.getGradedAt())
                .gradedByName(submission.getGradedBy() != null ? submission.getGradedBy().getName() : null)
                .build();
    }
}
