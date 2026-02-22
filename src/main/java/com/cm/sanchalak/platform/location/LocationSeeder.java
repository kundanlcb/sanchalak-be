package com.cm.sanchalak.platform.location;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after other seeders
public class LocationSeeder implements CommandLineRunner {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seeding Location Data...");
        seedIndia();
        log.info("Location Data Seeding Completed.");
    }

    private void seedIndia() {
        // Country: India
        Country india = countryRepository.findByCode("IN")
                .orElseGet(() -> countryRepository.save(
                        Country.builder().name("India").code("IN").isActive(true).build()));

        // State: Bihar
        State bihar = stateRepository.findByCodeAndCountryId("BR", india.getId())
                .orElseGet(() -> stateRepository.save(
                        State.builder().name("Bihar").code("BR").country(india).isActive(true).build()));

        // All 38 districts of Bihar as cities
        List<String> biharDistricts = Arrays.asList(
                "Araria", "Arwal", "Aurangabad", "Banka", "Begusarai",
                "Bhagalpur", "Bhojpur", "Buxar", "Darbhanga", "East Champaran",
                "Gaya", "Gopalganj", "Jamui", "Jehanabad", "Kaimur",
                "Katihar", "Khagaria", "Kishanganj", "Lakhisarai", "Madhepura",
                "Madhubani", "Munger", "Muzaffarpur", "Nalanda", "Nawada",
                "Patna", "Purnia", "Rohtas", "Saharsa", "Samastipur",
                "Saran", "Sheikhpura", "Sheohar", "Sitamarhi", "Siwan",
                "Supaul", "Vaishali", "West Champaran");

        long existingCities = cityRepository.findByStateIdAndIsActiveTrueOrderByNameAsc(bihar.getId()).size();
        if (existingCities == 0) {
            for (String district : biharDistricts) {
                cityRepository.save(
                        City.builder().name(district).state(bihar).isActive(true).build());
            }
            log.info("Seeded {} Bihar districts as cities.", biharDistricts.size());
        } else {
            log.info("Bihar cities already seeded ({} found). Skipping.", existingCities);
        }
    }
}
