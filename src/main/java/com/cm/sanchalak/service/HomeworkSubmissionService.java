package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.HomeworkSubmissionDto;
import com.cm.sanchalak.dto.PresignedUrlDto;
import com.cm.sanchalak.entity.Homework;
import com.cm.sanchalak.entity.HomeworkSubmission;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.repository.HomeworkRepository;
import com.cm.sanchalak.repository.HomeworkSubmissionRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.spec.HomeworkSpecification;
import com.cm.sanchalak.repository.spec.HomeworkSubmissionSpecification;
import com.cm.sanchalak.repository.spec.StudentSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class HomeworkSubmissionService {

        private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
                        "image/jpeg", "image/jpg", "image/png", "application/pdf");
        private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                        "jpg", "jpeg", "png", "pdf");

        private final HomeworkSubmissionRepository submissionRepository;
        private final HomeworkRepository homeworkRepository;
        private final StudentRepository studentRepository;
        private final FileStorageService fileStorageService;
        private final OwnershipValidator ownership;

        /**
         * Generate presigned upload URL for homework submission file
         */
        public PresignedUrlDto generateUploadUrl(Long homeworkId, Long studentId, String fileName, String contentType) {
                validateFileType(contentType, fileName);

                Homework homework = homeworkRepository.findOne(HomeworkSpecification.activeById(homeworkId))
                                .orElseThrow(() -> new IllegalArgumentException("Homework not found or unauthorized"));

                Student student = studentRepository.findOne(StudentSpecification.activeById(studentId))
                                .orElseThrow(() -> new IllegalArgumentException("Student not found or unauthorized"));

                String extension = getFileExtension(fileName);
                String objectKey = String.format("homework/%d/student_%d/%s.%s",
                                homeworkId, studentId, UUID.randomUUID().toString(), extension);

                String uploadUrl = fileStorageService.generateUploadUrl(objectKey, contentType, 15);

                log.info("Generated upload URL for homework: {} ({}), student: {} ({})",
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
                Homework homework = homeworkRepository.findOne(HomeworkSpecification.activeById(homeworkId))
                                .orElseThrow(() -> new IllegalArgumentException("Homework not found or unauthorized"));

                Student student = studentRepository.findOne(StudentSpecification.activeById(studentId))
                                .orElseThrow(() -> new IllegalArgumentException("Student not found or unauthorized"));

                Optional<HomeworkSubmission> existingOpt = submissionRepository.findOne(HomeworkSubmissionSpecification
                                .activeScoped()
                                .and((root, query, cb) -> cb.equal(root.get("homework").get("id"), homeworkId))
                                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId)));

                HomeworkSubmission submission;
                if (existingOpt.isPresent()) {
                        submission = existingOpt.get();
                        submission.setStatus(HomeworkSubmission.SubmissionStatus.RESUBMITTED);
                        submission.setSubmissionFileUrls(fileUrls);
                        submission.setStudentRemarks(remarks);
                        submission.setSubmittedAt(Instant.now());

                        log.info("Homework resubmitted: homework={}, student={}", homeworkId, studentId);
                } else {
                        submission = new HomeworkSubmission();
                        submission.setHomework(homework);
                        submission.setStudent(student);
                        submission.setSubmissionFileUrls(fileUrls);
                        submission.setStudentRemarks(remarks);
                        submission.setSubmittedAt(Instant.now());
                        submission.setStatus(HomeworkSubmission.SubmissionStatus.SUBMITTED);

                        log.info("Homework submitted: homework={}, student={}", homeworkId, studentId);
                }

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
                return submissionRepository.findOne(HomeworkSubmissionSpecification.activeScoped()
                                .and((root, query, cb) -> cb.equal(root.get("homework").get("id"), homeworkId))
                                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId)))
                                .map(this::mapToDto);
        }

        /**
         * Get all submissions for a students
         */
        @Transactional(readOnly = true)
        public List<HomeworkSubmissionDto> getStudentSubmissions(Long studentId) {
                return submissionRepository.findAll(HomeworkSubmissionSpecification.activeScoped()
                                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId))).stream()
                                .map(this::mapToDto)
                                .toList();
        }

        private void validateFileType(String contentType, String fileName) {
                if (!ALLOWED_FILE_TYPES.contains(contentType.toLowerCase())) {
                        throw new IllegalArgumentException(
                                        String.format("File type not allowed. Allowed types: %s",
                                                        String.join(", ", ALLOWED_FILE_TYPES)));
                }

                String extension = getFileExtension(fileName).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                        throw new IllegalArgumentException(
                                        String.format("File extension not allowed. Allowed extensions: %s",
                                                        String.join(", ", ALLOWED_EXTENSIONS)));
                }
        }

        private String getFileExtension(String fileName) {
                int lastDot = fileName.lastIndexOf('.');
                if (lastDot == -1) {
                        throw new IllegalArgumentException("File name must have an extension");
                }
                return fileName.substring(lastDot + 1);
        }

        private boolean checkIfLate(LocalDate dueDate, Instant submittedAt) {
                LocalDate submittedDate = submittedAt.atZone(ZoneId.systemDefault()).toLocalDate();
                return submittedDate.isAfter(dueDate);
        }

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
                                .gradedByName(submission.getGradedBy() != null ? submission.getGradedBy().getName()
                                                : null)
                                .build();
        }
}
