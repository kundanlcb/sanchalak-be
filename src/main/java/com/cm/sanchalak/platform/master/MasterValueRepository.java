package com.cm.sanchalak.platform.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterValueRepository extends JpaRepository<MasterValue, Long> {
    List<MasterValue> findByDomainCode(String domainCode);

    List<MasterValue> findByDomainCodeAndIsActiveTrueOrderBySortOrderAsc(String domainCode);

    Optional<MasterValue> findByDomainCodeAndCode(String domainCode, String code);

    boolean existsByDomainCodeAndCode(String domainCode, String code);
}
