package com.cm.sanchalak.platform.importing;

import com.cm.sanchalak.platform.school.SchoolRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;

@Service
public class ImportService {

    private final ImportJobRepository importJobRepository;
    private final SchoolRepository schoolRepository;

    public ImportService(ImportJobRepository importJobRepository, SchoolRepository schoolRepository) {
        this.importJobRepository = importJobRepository;
        this.schoolRepository = schoolRepository;
    }

    @Transactional
    public ImportJob createImportJob(UUID schoolId, ImportType type, MultipartFile file) {
        // Validation: Check if school exists
        if (!schoolRepository.existsById(schoolId)) {
            throw new RuntimeException("School not found");
        }

        // 1. Upload file to storage (S3/Local) - Mocking for now
        String fileUrl = "mock_url/" + file.getOriginalFilename();

        ImportJob job = new ImportJob();
        job.setSchoolId(schoolId);
        job.setType(type);
        job.setFileUrl(fileUrl);
        job.setStatus(ImportStatus.PENDING);

        return importJobRepository.save(job);
    }

    @Async
    public void processImport(UUID jobId, MultipartFile file) {
        ImportJob job = importJobRepository.findById(jobId).orElseThrow();
        job.setStatus(ImportStatus.PROCESSING);
        importJobRepository.save(job);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                org.apache.commons.csv.CSVParser csvParser = new org.apache.commons.csv.CSVParser(reader,
                        org.apache.commons.csv.CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            int count = 0;
            int failed = 0;

            for (org.apache.commons.csv.CSVRecord csvRecord : csvParser) {
                try {
                    switch (job.getType()) {
                        case STUDENT:
                            processStudentRecord(job.getSchoolId(), csvRecord);
                            break;
                        case TEACHER:
                            processTeacherRecord(job.getSchoolId(), csvRecord);
                            break;
                        case PARENT:
                            processParentRecord(job.getSchoolId(), csvRecord);
                            break;
                    }
                    count++;
                } catch (Exception e) {
                    failed++;
                    // Log error to a file/list for error report
                }
            }

            job.setTotalRecords(count + failed);
            job.setProcessedRecords(count);
            job.setFailedRecords(failed);
            job.setStatus(ImportStatus.COMPLETED);

        } catch (Exception e) {
            job.setStatus(ImportStatus.FAILED);
            job.setErrorReportUrl("Error: " + e.getMessage());
        }
        importJobRepository.save(job);
    }

    private void processStudentRecord(UUID schoolId, org.apache.commons.csv.CSVRecord record) {
        // Validation and saving logic
        // String name = record.get("Name");
        // ...
    }

    private void processTeacherRecord(UUID schoolId, org.apache.commons.csv.CSVRecord record) {
        // Validation and saving logic
    }

    private void processParentRecord(UUID schoolId, org.apache.commons.csv.CSVRecord record) {
        // Validation and saving logic
    }
}
