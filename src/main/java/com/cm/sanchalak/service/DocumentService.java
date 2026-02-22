package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.CreateDocumentRequest;
import com.cm.sanchalak.dto.PresignedUrlDto;
import com.cm.sanchalak.dto.StudentDocumentDto;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.StudentDocument;
import com.cm.sanchalak.repository.StudentDocumentRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.spec.DocumentSpecification;
import com.cm.sanchalak.repository.spec.StudentSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final StudentDocumentRepository documentRepository;
    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;
    private final OwnershipValidator ownership;

    public PresignedUrlDto generateUploadUrl(String fileName, String mimeType) {
        String objectKey = "documents/" + UUID.randomUUID() + "/" + fileName;
        String url = fileStorageService.generateUploadUrl(objectKey, mimeType, 15);
        return PresignedUrlDto.builder()
                .uploadUrl(url)
                .objectKey(objectKey)
                .expiryMinutes(15)
                .instructions("Upload file using PUT to uploadUrl with Content-Type: " + mimeType)
                .build();
    }

    @Transactional
    public StudentDocumentDto createDocument(CreateDocumentRequest request) {
        Student student = studentRepository.findOne(StudentSpecification.activeById(request.getStudentId()))
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentDocument doc = new StudentDocument();
        doc.setStudent(student);
        doc.setDocumentType(request.getDocumentType());
        doc.setFileName(request.getFileName());
        doc.setFileUrl(request.getFileUrl());
        doc.setMimeType(request.getMimeType());
        doc.setFileSize(request.getFileSize());
        doc.setDescription(request.getDescription());

        doc = documentRepository.save(doc);

        return mapToDto(doc);
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentDto> getDocuments(Long studentId) {
        studentRepository.findOne(StudentSpecification.activeById(studentId))
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<StudentDocument> docs = documentRepository.findAll(DocumentSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId)));

        return docs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long id) {
        StudentDocument doc = documentRepository.findOne(DocumentSpecification.activeById(id))
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try {
            fileStorageService.deleteFile(doc.getFileUrl());
        } catch (Exception e) {
            log.error("Failed to delete file from storage: " + doc.getFileUrl(), e);
        }

        documentRepository.delete(doc);
    }

    private StudentDocumentDto mapToDto(StudentDocument doc) {
        StudentDocumentDto dto = new StudentDocumentDto();
        dto.setId(doc.getId());
        dto.setStudentId(doc.getStudent().getId());
        dto.setDocumentType(doc.getDocumentType());
        dto.setFileName(doc.getFileName());
        dto.setFileUrl(fileStorageService.getPublicUrl(doc.getFileUrl()));
        dto.setDescription(doc.getDescription());
        if (doc.getCreatedAt() != null) {
            dto.setUploadedAt(LocalDateTime.ofInstant(doc.getCreatedAt(), ZoneId.systemDefault()));
        }
        return dto;
    }
}
