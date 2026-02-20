package com.cm.sanchalak.platform.subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class FeatureSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FeatureSeeder.class);
    private final FeatureRepository featureRepository;

    public FeatureSeeder(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (featureRepository.count() > 0) {
            logger.info("Features already seeded. Skipping initialization.");
            return;
        }

        logger.info("Seeding master feature list...");

        List<Feature> masterFeatures = Arrays.asList(
                createFeature("STUDENT_MGMT", "Student Management",
                        "Manage student profiles, admissions, and records."),
                createFeature("TEACHER_MGMT", "Teacher Management", "Manage staff profiles, roles, and assignments."),
                createFeature("ATTENDANCE", "Attendance Tracking", "Track daily student and staff attendance."),
                createFeature("TIMETABLE", "Timetable Generation",
                        "Create and manage academic schedules and timetables."),
                createFeature("EXAM_MGMT", "Exam Management", "Define exams, record marks, and generate report cards."),
                createFeature("FINANCE", "Finance & Payroll",
                        "Manage fees, expenses, employee payroll, and financial reports."),
                createFeature("LIBRARY", "Library Management", "Track books, issuances, returns, and library fines."),
                createFeature("TRANSPORT", "Transport Management",
                        "Manage vehicle routes, stops, and transport assignments."),
                createFeature("HOSTEL", "Hostel Management", "Manage hostel rooms, allocation, and boarding details."),
                createFeature("COMMUNICATION", "Communication (SMS/Email)",
                        "Broadcast messages, notifications, and alerts."),
                createFeature("MOBILE_APP", "Mobile App Access", "Enable access to the companion mobile application."),
                createFeature("ANALYTICS", "Analytics Dashboard", "Access advanced reporting and school analytics."),
                createFeature("PRIORITY_SUPPORT", "Priority Support",
                        "24/7 priority customer support for the institution."));

        featureRepository.saveAll(masterFeatures);
        logger.info("Successfully seeded {} features.", masterFeatures.size());
    }

    private Feature createFeature(String code, String name, String description) {
        Feature f = new Feature();
        f.setCode(code);
        f.setName(name);
        f.setDescription(description);
        return f;
    }
}
