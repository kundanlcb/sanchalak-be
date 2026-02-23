package com.cm.sanchalak.repository;

import com.cm.sanchalak.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findBySchoolIdOrderByOrderIndexAsc(java.util.UUID schoolId);

    void deleteBySchoolId(java.util.UUID schoolId);
}
