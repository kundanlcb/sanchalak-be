package com.cm.sanchalak.platform.location;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    // DTO records for clean API responses (no lazy-loading issues)
    public record CountryDto(Long id, String name, String code) {
    }

    public record StateDto(Long id, String name, String code) {
    }

    public record CityDto(Long id, String name) {
    }

    @GetMapping("/countries")
    public ResponseEntity<List<CountryDto>> getCountries() {
        List<CountryDto> countries = countryRepository.findByIsActiveTrue().stream()
                .map(c -> new CountryDto(c.getId(), c.getName(), c.getCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/countries/{countryId}/states")
    public ResponseEntity<List<StateDto>> getStates(@PathVariable Long countryId) {
        List<StateDto> states = stateRepository.findByCountryIdAndIsActiveTrue(countryId).stream()
                .map(s -> new StateDto(s.getId(), s.getName(), s.getCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(states);
    }

    @GetMapping("/states/{stateId}/cities")
    public ResponseEntity<List<CityDto>> getCities(@PathVariable Long stateId) {
        List<CityDto> cities = cityRepository.findByStateIdAndIsActiveTrueOrderByNameAsc(stateId).stream()
                .map(c -> new CityDto(c.getId(), c.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(cities);
    }
}
