package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.DocumentTemplateDto;
import com.cm.sanchalak.entity.DocumentTemplate;
import com.cm.sanchalak.repository.DocumentTemplateRepository;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentTemplateService {

    private final DocumentTemplateRepository templateRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public DocumentTemplateDto getTemplate() {
        UUID schoolId = SchoolContext.getSchoolId();
        return templateRepository.findBySchoolId(schoolId)
                .map(this::mapToDto)
                .orElse(new DocumentTemplateDto()); // safe empty default
    }

    @Transactional
    public DocumentTemplateDto saveTemplate(DocumentTemplateDto dto) {
        UUID schoolId = SchoolContext.getSchoolId();
        DocumentTemplate template = templateRepository.findBySchoolId(schoolId)
                .orElse(DocumentTemplate.builder().schoolId(schoolId).build());

        template.setSchoolName(dto.getSchoolName());
        template.setAddressLine1(dto.getAddressLine1());
        template.setAddressLine2(dto.getAddressLine2());
        template.setPhone1(dto.getPhone1());
        template.setPhone2(dto.getPhone2());
        template.setRegNo(dto.getRegNo());
        template.setSchoolCode(dto.getSchoolCode());
        template.setPrimaryColorHex(dto.getPrimaryColorHex());
        template.setAdmitCardFooterNote(dto.getAdmitCardFooterNote());
        template.setFeeReceiptFooterNote(dto.getFeeReceiptFooterNote());
        template.setControllerDesignation(dto.getControllerDesignation());
        template.setPrincipalDesignation(dto.getPrincipalDesignation());
        if (dto.getLogoUrl() != null)
            template.setLogoUrl(dto.getLogoUrl());

        return mapToDto(templateRepository.save(template));
    }

    /**
     * Generate presigned URL for logo upload via existing FileStorageService.
     */
    public String getLogoUploadUrl(String fileName, String contentType) {
        UUID schoolId = SchoolContext.getSchoolId();
        String objectKey = "school/" + schoolId + "/logo/" + fileName;
        return fileStorageService.generateUploadUrl(objectKey, contentType, 15);
    }

    private DocumentTemplateDto mapToDto(DocumentTemplate t) {
        DocumentTemplateDto dto = new DocumentTemplateDto();
        dto.setId(t.getId());
        dto.setSchoolName(t.getSchoolName());
        dto.setAddressLine1(t.getAddressLine1());
        dto.setAddressLine2(t.getAddressLine2());
        dto.setPhone1(t.getPhone1());
        dto.setPhone2(t.getPhone2());
        dto.setRegNo(t.getRegNo());
        dto.setSchoolCode(t.getSchoolCode());
        dto.setPrimaryColorHex(t.getPrimaryColorHex());
        dto.setAdmitCardFooterNote(t.getAdmitCardFooterNote());
        dto.setFeeReceiptFooterNote(t.getFeeReceiptFooterNote());
        dto.setControllerDesignation(t.getControllerDesignation());
        dto.setPrincipalDesignation(t.getPrincipalDesignation());
        dto.setLogoUrl(t.getLogoUrl() != null ? fileStorageService.getPublicUrl(t.getLogoUrl()) : null);
        return dto;
    }
}
