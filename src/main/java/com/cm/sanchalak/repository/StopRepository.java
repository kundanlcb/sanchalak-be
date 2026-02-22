package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, Long>, JpaSpecificationExecutor<Stop> {
    List<Stop> findByRouteIdOrderByStopOrder(Long routeId);
}
