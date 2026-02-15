package com.cm.sanchalak.platform.academic;

import com.cm.sanchalak.entity.AcademicYear;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.Section;
import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AcademicStructureService {

    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;

    public AcademicStructureService(AcademicYearRepository academicYearRepository,
            SchoolClassRepository schoolClassRepository, SectionRepository sectionRepository,
            SubjectRepository subjectRepository) {
        this.academicYearRepository = academicYearRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
    }

    // Academic Year
    @Transactional
    public AcademicYear createAcademicYear(AcademicYear academicYear) {
        // Enforce only one current year per school (simplification)
        if (academicYear.isCurrent()) {
            List<AcademicYear> existing = academicYearRepository.findBySchoolId(academicYear.getSchoolId());
            existing.forEach(y -> {
                if (y.isCurrent()) {
                    y.setCurrent(false);
                    academicYearRepository.save(y);
                }
            });
        }
        return academicYearRepository.save(academicYear);
    }

    public List<AcademicYear> getAcademicYears(UUID schoolId) {
        return academicYearRepository.findBySchoolId(schoolId);
    }

    // School Class
    @Transactional
    public SchoolClass createClass(SchoolClass schoolClass) {
        return schoolClassRepository.save(schoolClass);
    }

    public List<SchoolClass> getClasses(UUID schoolId) {
        return schoolClassRepository.findBySchoolId(schoolId);
    }

    // Section
    @Transactional
    public Section createSection(Section section) {
        return sectionRepository.save(section);
    }

    public List<Section> getSections(Long classId) {
        return sectionRepository.findBySchoolClassId(classId);
    }

    // Subject
    @Transactional
    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public List<Subject> getSubjects(UUID schoolId) {
        return subjectRepository.findBySchoolId(schoolId);
    }
}
