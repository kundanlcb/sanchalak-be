package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.StudentImportStaging;
import com.cm.sanchalak.repository.StudentImportStagingRepository;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentImportService {

    private final OwnershipValidator ownership;
    private final StudentImportStagingRepository stagingRepository;

    @Transactional
    public int importStudents(MultipartFile file) {
        UUID schoolId = SchoolContext.getSchoolId();
        ownership.validate(schoolId);

        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(fileReader,
                        CSVFormat.Builder.create(CSVFormat.DEFAULT)
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreHeaderCase(true)
                                .setTrim(true)
                                .build())) {

            List<StudentImportStaging> stagingRecords = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                StudentImportStaging record = new StudentImportStaging();
                record.setFirstName(
                        csvRecord.isMapped("firstName") ? csvRecord.get("firstName") : csvRecord.get("fullName"));
                record.setLastName(csvRecord.isMapped("lastName") ? csvRecord.get("lastName") : null);
                record.setEmail(csvRecord.get("email"));
                record.setPhone(csvRecord.get("phone"));
                record.setAdmissionNo(csvRecord.get("admissionNo"));
                record.setClassName(csvRecord.get("className"));
                record.setParentName(csvRecord.isMapped("parentName") ? csvRecord.get("parentName") : null);
                record.setParentPhone(csvRecord.isMapped("parentPhone") ? csvRecord.get("parentPhone") : null);
                record.setSchoolId(schoolId);

                stagingRecords.add(record);
            }

            stagingRepository.saveAll(stagingRecords);
            return stagingRecords.size();

        } catch (Exception e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }
}
