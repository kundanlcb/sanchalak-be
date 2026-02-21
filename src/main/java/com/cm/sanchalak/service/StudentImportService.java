package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.StudentRequest;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentImportService {

    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public int importStudents(MultipartFile file) {
        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(fileReader,
                        CSVFormat.Builder.create(CSVFormat.DEFAULT)
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreHeaderCase(true)
                                .setTrim(true)
                                .build())) {

            List<Student> students = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            // Cache Role lookup outside the loop
            Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Student Role not set."));

            for (CSVRecord csvRecord : csvRecords) {
                String fullName = csvRecord.get("fullName");
                String email = csvRecord.get("email");
                String phone = csvRecord.get("phone");
                String admissionNo = csvRecord.get("admissionNo");
                String className = csvRecord.get("className"); // or classId
                // Optional parent info - NOW MAPPED
                String parentName = csvRecord.isMapped("parentName") ? csvRecord.get("parentName") : null;
                String parentPhone = csvRecord.isMapped("parentPhone") ? csvRecord.get("parentPhone") : null;

                // Create User account first
                if (userRepository.existsByEmail(email)) {
                    log.warn("Skipping student with existing email: {}", email);
                    continue;
                }

                User user = new User();
                user.setName(fullName);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode("password")); // Default password
                user.setMobileNumber(phone);
                user.setRoles(Collections.singleton(studentRole));

                user = userRepository.save(user);

                // Create Student Profile
                Student student = new Student();
                student.setUserId(user.getId());
                student.setName(fullName); // Keep full name for backward compatibility
                student.setEmail(email);

                // Split Name for consistency
                if (fullName != null) {
                    fullName = fullName.trim();
                    int lastSpaceIdx = fullName.lastIndexOf(" ");
                    if (lastSpaceIdx > 0) {
                        student.setFirstName(fullName.substring(0, lastSpaceIdx));
                        student.setLastName(fullName.substring(lastSpaceIdx + 1));
                    } else {
                        student.setFirstName(fullName);
                        student.setLastName("");
                    }
                }

                student.setAdmissionNumber(admissionNo);

                // Set Guardian Info
                if (parentName != null && !parentName.isEmpty())
                    student.setGuardianName(parentName);
                if (parentPhone != null && !parentPhone.isEmpty())
                    student.setGuardianMobile(parentPhone);

                if (className != null && !className.isEmpty()) {
                    Optional<SchoolClass> schoolClass = classRepository.findByName(className);
                    if (schoolClass.isPresent()) {
                        student.setStudentClass(schoolClass.get());
                    } else {
                        log.warn("Class not found: {}", className);
                    }
                }

                students.add(student);
            }

            studentRepository.saveAll(students);
            return students.size();

        } catch (Exception e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }
}
