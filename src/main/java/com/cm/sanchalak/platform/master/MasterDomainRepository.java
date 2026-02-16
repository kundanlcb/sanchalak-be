package com.cm.sanchalak.platform.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MasterDomainRepository extends JpaRepository<MasterDomain, UUID> {
    Optional<MasterDomain> findByCode(String code);

    boolean existsByCode(String code);
}
