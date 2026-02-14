package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.CreateDocumentRequest;
import com.cm.sanchalak.dto.PresignedUrlDto;
import com.cm.sanchalak.dto.StudentDocumentDto;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.StudentDocument;
import com.cm.sanchalak.repository.StudentDocumentRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        Student student = studentRepository.findById(request.getStudentId())
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

    public List<StudentDocumentDto> getDocuments(Long studentId) {
        List<StudentDocument> docs = documentRepository.findByStudentId(studentId);
        return docs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long id) {
        StudentDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Delete from S3/Storage
        try {
            fileStorageService.deleteFile(doc.getFileUrl());
        } catch (Exception e) {
            log.error("Failed to delete file from storage: " + doc.getFileUrl(), e);
            // Continue to delete record anyway
        }
        
        documentRepository.delete(doc);
    }

    private StudentDocumentDto mapToDto(StudentDocument doc) {
        StudentDocumentDto dto = new StudentDocumentDto();
        dto.setId(doc.getId());
        dto.setStudentId(doc.getStudent().getId());
        dto.setDocumentType(doc.getDocumentType());
        dto.setFileName(doc.getFileName());
        dto.setFileUrl(fileStorageService.getPublicUrl(doc.getFileUrl())); // Convert key to URL if needed, or return key
        // Note: Presigned URL generator might return full URL for upload, but stored key is relative. 
        // getPublicUrl constructs full URL.
        dto.setDescription(doc.getDescription());
        if (doc.getCreatedAt() != null) {
            dto.setUploadedAt(LocalDateTime.ofInstant(doc.getCreatedAt(), java.time.ZoneId.systemDefault()));
        }
        return dto;
    }
}
