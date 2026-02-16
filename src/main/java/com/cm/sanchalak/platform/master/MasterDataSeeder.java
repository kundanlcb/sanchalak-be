package com.cm.sanchalak.platform.master;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterDataSeeder implements CommandLineRunner {

    private final MasterDomainRepository domainRepository;
    private final MasterValueRepository valueRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seeding Master Data...");
        seedDomainsAndValues();
        log.info("Master Data Seeding Completed.");
    }

    private void seedDomainsAndValues() {
        seedDomain("GENDER", "Gender Identity", Arrays.asList(
                new ValueTuple("MALE", "Male"),
                new ValueTuple("FEMALE", "Female"),
                new ValueTuple("OTHER", "Other")));

        seedDomain("BLOOD_GROUP", "Blood Group", Arrays.asList(
                new ValueTuple("A_POS", "A+"),
                new ValueTuple("A_NEG", "A-"),
                new ValueTuple("B_POS", "B+"),
                new ValueTuple("B_NEG", "B-"),
                new ValueTuple("O_POS", "O+"),
                new ValueTuple("O_NEG", "O-"),
                new ValueTuple("AB_POS", "AB+"),
                new ValueTuple("AB_NEG", "AB-")));

        seedDomain("RELATION", "Parent/Guardian Relationship", Arrays.asList(
                new ValueTuple("FATHER", "Father"),
                new ValueTuple("MOTHER", "Mother"),
                new ValueTuple("GUARDIAN", "Guardian")));

        seedDomain("CATEGORY", "Social Category", Arrays.asList(
                new ValueTuple("GENERAL", "General"),
                new ValueTuple("OBC", "OBC"),
                new ValueTuple("SC", "SC"),
                new ValueTuple("ST", "ST")));

        seedDomain("RELIGION", "Religion", Arrays.asList(
                new ValueTuple("HINDU", "Hindu"),
                new ValueTuple("MUSLIM", "Muslim"),
                new ValueTuple("CHRISTIAN", "Christian"),
                new ValueTuple("SIKH", "Sikh"),
                new ValueTuple("BUDDHIST", "Buddhist"),
                new ValueTuple("JAIN", "Jain"),
                new ValueTuple("OTHER", "Other")));

        seedDomain("OCCUPATION", "Occupation", Arrays.asList(
                new ValueTuple("BUSINESS", "Business"),
                new ValueTuple("SERVICE_PRIVATE", "Private Service"),
                new ValueTuple("SERVICE_GOVT", "Government Service"),
                new ValueTuple("FARMER", "Farmer"),
                new ValueTuple("HOMEMAKER", "Homemaker"),
                new ValueTuple("OTHER", "Other")));
    }

    private void seedDomain(String code, String description, List<ValueTuple> values) {
        MasterDomain domain = domainRepository.findByCode(code)
                .orElseGet(() -> domainRepository.save(
                        MasterDomain.builder()
                                .code(code)
                                .description(description)
                                .isSystem(true)
                                .isSchoolScoped(false)
                                .build()));

        int sortOrder = 1;
        for (ValueTuple value : values) {
            if (!valueRepository.existsByDomainCodeAndCode(code, value.code())) {
                valueRepository.save(MasterValue.builder()
                        .domain(domain)
                        .code(value.code())
                        .label(value.label())
                        .isActive(true)
                        .sortOrder(sortOrder)
                        .build());
            }
            sortOrder++;
        }
    }

    private record ValueTuple(String code, String label) {
    }
}
