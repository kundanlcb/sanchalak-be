package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {
    List<Route> findBySchoolId(UUID schoolId);

    Optional<Route> findByRouteNameAndSchoolId(String routeName, UUID schoolId);
}
