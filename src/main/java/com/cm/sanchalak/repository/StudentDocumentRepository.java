package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentDocumentRepository
        extends JpaRepository<StudentDocument, Long>, JpaSpecificationExecutor<StudentDocument> {
    List<StudentDocument> findByStudentId(Long studentId);
}
