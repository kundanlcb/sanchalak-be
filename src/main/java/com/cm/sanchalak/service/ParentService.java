package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.LinkedStudentDto;
import com.cm.sanchalak.entity.Parent;
import com.cm.sanchalak.entity.ParentStudentLink;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.ParentRepository;
import com.cm.sanchalak.repository.ParentStudentLinkRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for parent profile and linked students management
 */
@Service
@RequiredArgsConstructor
public class ParentService {

    private static final Logger logger = LoggerFactory.getLogger(ParentService.class);

    private final ParentRepository parentRepository;
    private final ParentStudentLinkRepository linkRepository;

    /**
     * Get parent by user ID
     */
    @Transactional(readOnly = true)
    public Optional<Parent> getParentByUserId(UUID userId) {
        return parentRepository.findByUserId(userId);
    }

    /**
     * Get parent by user
     */
    @Transactional(readOnly = true)
    public Optional<Parent> getParentByUser(User user) {
        return parentRepository.findByUser(user);
    }

    /**
     * Get all students linked to a parent
     */
    @Transactional(readOnly = true)
    public List<LinkedStudentDto> getLinkedStudents(Long parentId) {
        logger.info("Fetching linked students for parent: {}", parentId);

        Optional<Parent> parentOpt = parentRepository.findById(parentId);
        if (parentOpt.isEmpty()) {
            throw new IllegalArgumentException("Parent not found");
        }

        Parent parent = parentOpt.get();
        List<ParentStudentLink> links = linkRepository.findByParentAndIsActiveTrue(parent);

        return links.stream()
                .map(this::mapToLinkedStudentDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all students linked to a parent by user ID
     */
    @Transactional(readOnly = true)
    public List<LinkedStudentDto> getLinkedStudentsByUserId(UUID userId) {
        Optional<Parent> parentOpt = parentRepository.findByUserId(userId);
        if (parentOpt.isEmpty()) {
            throw new IllegalArgumentException("Parent not found for user");
        }

        return getLinkedStudents(parentOpt.get().getId());
    }

    /**
     * Map ParentStudentLink to LinkedStudentDto
     */
    private LinkedStudentDto mapToLinkedStudentDto(ParentStudentLink link) {
        var student = link.getStudent();

        return LinkedStudentDto.builder()
                .studentId(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(student.getName())
                .className(student.getStudentClass() != null ? student.getStudentClass().getName() : null)
                .rollNo(student.getRollNo())
                .rollNumber(student.getRollNo())
                .relationshipType(link.getRelationshipType())
                .isPrimary(link.getIsPrimary())
                .isActive(link.getIsActive())
                .build();
    }

    /**
     * Check if parent exists for user
     */
    @Transactional(readOnly = true)
    public boolean existsByUser(User user) {
        return parentRepository.existsByUser(user);
    }
}
