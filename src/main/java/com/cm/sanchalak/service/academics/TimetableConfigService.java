package com.cm.sanchalak.service.academics;

import com.cm.sanchalak.dto.academics.TimetableSlotDto;
import com.cm.sanchalak.entity.TimetableSlot;
import com.cm.sanchalak.repository.TimetableSlotRepository;
import com.cm.sanchalak.security.SchoolContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableConfigService {

    private final TimetableSlotRepository timetableSlotRepository;

    @Transactional(readOnly = true)
    public List<TimetableSlotDto> getSchoolTimetableSlots() {
        java.util.UUID schoolId = SchoolContext.getSchoolId();
        List<TimetableSlot> slots = timetableSlotRepository.findBySchoolIdOrderByOrderIndexAsc(schoolId);

        return slots.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public List<TimetableSlotDto> updateSchoolTimetableSlots(List<TimetableSlotDto> slotDtos) {
        java.util.UUID schoolId = SchoolContext.getSchoolId();

        // Strategy: Full replacement for simplicity
        timetableSlotRepository.deleteBySchoolId(schoolId);

        List<TimetableSlot> newSlots = slotDtos.stream().map(dto -> mapToEntity(dto, schoolId))
                .collect(Collectors.toList());
        List<TimetableSlot> savedSlots = timetableSlotRepository.saveAll(newSlots);

        return savedSlots.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private TimetableSlotDto mapToDto(TimetableSlot entity) {
        return TimetableSlotDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .isBreak(entity.getIsBreak())
                .orderIndex(entity.getOrderIndex())
                .build();
    }

    private TimetableSlot mapToEntity(TimetableSlotDto dto, java.util.UUID schoolId) {
        return TimetableSlot.builder()
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .isBreak(dto.getIsBreak() != null ? dto.getIsBreak() : false)
                .orderIndex(dto.getOrderIndex())
                .schoolId(schoolId)
                .build();
    }
}
