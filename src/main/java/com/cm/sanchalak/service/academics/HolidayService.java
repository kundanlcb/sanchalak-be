package com.cm.sanchalak.service.academics;

import com.cm.sanchalak.dto.academics.HolidayDto;
import java.util.List;

public interface HolidayService {
    List<HolidayDto> getAllHolidays(String academicYear);

    HolidayDto createHoliday(HolidayDto holidayDto);

    HolidayDto updateHoliday(Long id, HolidayDto holidayDto);

    void deleteHoliday(Long id);

    void seedNationalHolidays(String academicYear);
}
