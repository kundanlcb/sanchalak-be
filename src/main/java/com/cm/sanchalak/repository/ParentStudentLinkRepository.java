package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Parent;
import com.cm.sanchalak.entity.ParentStudentLink;
import com.cm.sanchalak.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentStudentLinkRepository
        extends JpaRepository<ParentStudentLink, Long>, JpaSpecificationExecutor<ParentStudentLink> {

    /**
     * Find all active students linked to a parent
     */
    List<ParentStudentLink> findByParentAndIsActiveTrue(Parent parent);

    /**
     * Find active linkage between parent and specific student
     */
    Optional<ParentStudentLink> findByParentIdAndStudentIdAndIsActiveTrue(
            Long parentId,
            Long studentId);

    /**
     * Custom query to validate parent-student linkage
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
            "FROM ParentStudentLink l " +
            "WHERE l.parent.id = :parentId " +
            "AND l.student.id = :studentId " +
            "AND l.isActive = true")
    boolean existsActiveByParentIdAndStudentId(
            @Param("parentId") Long parentId,
            @Param("studentId") Long studentId);

    /**
     * Find all parents linked to a student
     */
    List<ParentStudentLink> findByStudentAndIsActiveTrue(Student student);

    /**
     * Find all active parent-student links by student ID
     */
    List<ParentStudentLink> findByStudentIdAndIsActiveTrue(Long studentId);

    /**
     * Find all active parent-student links by parent ID
     */
    List<ParentStudentLink> findByParentIdAndIsActiveTrue(Long parentId);

    /**
     * Find primary parent for a student
     */
    Optional<ParentStudentLink> findByStudentAndIsPrimaryTrueAndIsActiveTrue(Student student);
}
