package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Parent;
import com.cm.sanchalak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long>, JpaSpecificationExecutor<Parent> {
    Optional<Parent> findByUserId(UUID userId);

    Optional<Parent> findByUser(User user);

    boolean existsByUser(User user);
}
