package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.LinkedStudentDto;
import com.cm.sanchalak.entity.Parent;
import com.cm.sanchalak.entity.ParentStudentLink;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.ParentRepository;
import com.cm.sanchalak.repository.ParentStudentLinkRepository;
import com.cm.sanchalak.repository.spec.ParentSpecification;
import com.cm.sanchalak.repository.spec.ParentStudentLinkSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ParentService {

    private final ParentRepository parentRepository;
    private final ParentStudentLinkRepository linkRepository;
    private final OwnershipValidator ownership;

    /**
     * Get parent by user ID
     */
    @Transactional(readOnly = true)
    public Optional<Parent> getParentByUserId(UUID userId) {
        return parentRepository.findOne(ParentSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("userId"), userId)));
    }

    /**
     * Get parent by user
     */
    @Transactional(readOnly = true)
    public Optional<Parent> getParentByUser(User user) {
        return getParentByUserId(user.getId());
    }

    /**
     * Get all students linked to a parent
     */
    @Transactional(readOnly = true)
    public List<LinkedStudentDto> getLinkedStudents(Long parentId) {
        log.info("Fetching linked students for parent: {}", parentId);

        Parent parent = parentRepository.findOne(ParentSpecification.activeById(parentId))
                .orElseThrow(() -> new IllegalArgumentException("Parent not found or unauthorized"));

        List<ParentStudentLink> links = linkRepository.findAll(ParentStudentLinkSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("parent").get("id"), parentId))
                .and((root, query, cb) -> cb.equal(root.get("isActive"), true)));

        return links.stream()
                .map(this::mapToLinkedStudentDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all students linked to a parent by user ID
     */
    @Transactional(readOnly = true)
    public List<LinkedStudentDto> getLinkedStudentsByUserId(UUID userId) {
        Parent parent = getParentByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found for user"));

        return getLinkedStudents(parent.getId());
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
        return getParentByUserId(user.getId()).isPresent();
    }
}
