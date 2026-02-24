package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.StudentImportStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentImportStagingRepository
                extends JpaRepository<StudentImportStaging, Long>, JpaSpecificationExecutor<StudentImportStaging> {
}
