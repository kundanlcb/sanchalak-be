package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.ApiEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiEndpointRepository extends JpaRepository<ApiEndpoint, Long> {

    Optional<ApiEndpoint> findByMethodAndUrlPattern(String method, String urlPattern);
}
