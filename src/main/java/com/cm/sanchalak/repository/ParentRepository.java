package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Parent;
import com.cm.sanchalak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    
    /**
     * Find parent by user account
     */
    Optional<Parent> findByUser(User user);
    
    /**
     * Find parent by user ID
     */
    Optional<Parent> findByUserId(UUID userId);
    
    /**
     * Find active parent by mobile number
     */
    Optional<Parent> findByMobileNumberAndIsActiveTrue(String mobileNumber);
    
    /**
     * Check if parent exists for user
     */
    boolean existsByUser(User user);
}
