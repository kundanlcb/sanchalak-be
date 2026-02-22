package com.cm.sanchalak.platform.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findByCountryIdAndIsActiveTrue(Long countryId);

    Optional<State> findByCodeAndCountryId(String code, Long countryId);
}
