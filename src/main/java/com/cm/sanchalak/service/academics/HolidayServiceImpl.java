package com.cm.sanchalak.service.academics;

import com.cm.sanchalak.dto.academics.HolidayDto;
import com.cm.sanchalak.entity.academics.Holiday;
import com.cm.sanchalak.entity.academics.HolidayType;
import com.cm.sanchalak.repository.HolidayRepository;
import com.cm.sanchalak.security.SchoolContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;

    @Override
    public List<HolidayDto> getAllHolidays(String academicYear) {
        String tenantId = null;
        try {
            java.util.UUID schoolId = SchoolContext.getSchoolId();
            if (schoolId != null) {
                tenantId = schoolId.toString();
            }
        } catch (Exception e) {
            // No school context
        }

        List<Holiday> holidays;
        if (tenantId == null) {
            // Usually requested by a sysadmin
            holidays = holidayRepository.findAll();
        } else {
            holidays = holidayRepository.findAllActiveHolidaysForTenant(tenantId, academicYear);
        }

        return holidays.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HolidayDto createHoliday(HolidayDto holidayDto) {
        String tenantId = null;
        try {
            java.util.UUID schoolId = SchoolContext.getSchoolId();
            if (schoolId != null) {
                tenantId = schoolId.toString();
            }
        } catch (Exception e) {
            // No school context
        }

        Holiday holiday = new Holiday();
        holiday.setName(holidayDto.getName());
        holiday.setStartDate(holidayDto.getStartDate());
        holiday.setEndDate(holidayDto.getEndDate());
        holiday.setType(holidayDto.getType() != null ? holidayDto.getType() : HolidayType.INSTITUTIONAL);
        holiday.setApplicableToStudents(holidayDto.isApplicableToStudents());
        holiday.setApplicableToStaff(holidayDto.isApplicableToStaff());
        holiday.setAcademicYear(holidayDto.getAcademicYear());

        // National holidays apply to all tenants, so tenantId is null
        if (holiday.getType() == HolidayType.NATIONAL) {
            holiday.setTenantId(null);
        } else {
            holiday.setTenantId(tenantId);
        }

        Holiday savedHoliday = holidayRepository.save(holiday);
        return mapToDto(savedHoliday);
    }

    @Override
    @Transactional
    public HolidayDto updateHoliday(Long id, HolidayDto holidayDto) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found with ID: " + id));

        // Note: For multi-tenancy security, ensure this tenant owns the holiday or it's
        // a global action.

        holiday.setName(holidayDto.getName());
        holiday.setStartDate(holidayDto.getStartDate());
        holiday.setEndDate(holidayDto.getEndDate());
        holiday.setApplicableToStudents(holidayDto.isApplicableToStudents());
        holiday.setApplicableToStaff(holidayDto.isApplicableToStaff());
        holiday.setAcademicYear(holidayDto.getAcademicYear());

        Holiday updatedHoliday = holidayRepository.save(holiday);
        return mapToDto(updatedHoliday);
    }

    @Override
    @Transactional
    public void deleteHoliday(Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new RuntimeException("Holiday not found with ID: " + id);
        }
        holidayRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void seedNationalHolidays(String academicYear) {
        log.info("Checking if National Holidays need to be seeded for academic year: {}", academicYear);

        long count = holidayRepository.countByTypeAndAcademicYear(HolidayType.NATIONAL, academicYear);
        if (count > 0) {
            log.info("National holidays already seeded for year {}", academicYear);
            return;
        }

        int year = LocalDate.now().getYear();

        // Sample Indian National Holidays - In reality, could be loaded from a config
        // or API
        createNationalHolidayHelper("Republic Day", LocalDate.of(year, 1, 26), academicYear);
        createNationalHolidayHelper("Independence Day", LocalDate.of(year, 8, 15), academicYear);
        createNationalHolidayHelper("Gandhi Jayanti", LocalDate.of(year, 10, 2), academicYear);

        log.info("Successfully seeded National Holidays for academic year: {}", academicYear);
    }

    private void createNationalHolidayHelper(String name, LocalDate date, String academicYear) {
        Holiday h = new Holiday();
        h.setName(name);
        h.setStartDate(date);
        h.setEndDate(date);
        h.setType(HolidayType.NATIONAL);
        h.setApplicableToStaff(true);
        h.setApplicableToStudents(true);
        h.setAcademicYear(academicYear);
        h.setTenantId(null); // Global
        holidayRepository.save(h);
    }

    private HolidayDto mapToDto(Holiday holiday) {
        HolidayDto dto = new HolidayDto();
        dto.setId(holiday.getId());
        dto.setTenantId(holiday.getTenantId());
        dto.setName(holiday.getName());
        dto.setStartDate(holiday.getStartDate());
        dto.setEndDate(holiday.getEndDate());
        dto.setType(holiday.getType());
        dto.setApplicableToStudents(holiday.isApplicableToStudents());
        dto.setApplicableToStaff(holiday.isApplicableToStaff());
        dto.setAcademicYear(holiday.getAcademicYear());
        return dto;
    }
}
