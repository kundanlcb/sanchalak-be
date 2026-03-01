package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.finance.*;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.repository.spec.FinanceSpecification;
import com.cm.sanchalak.repository.spec.SchoolClassSpecification;
import com.cm.sanchalak.repository.spec.StudentSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FinanceService {

    private final FeeCategoryRepository feeCategoryRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final StudentRepository studentRepository;
    private final ClassFeeAssignmentRepository classFeeAssignmentRepository;
    private final StudentFeeMapRepository studentFeeMapRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final FeeStructureItemRepository feeStructureItemRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptService receiptService;
    private final SchoolClassRepository schoolClassRepository;
    private final AuditLogService auditLogService;
    private final OwnershipValidator ownership;

    @CacheEvict(value = "fee-categories", allEntries = true)
    public FeeCategoryDto createCategory(UUID schoolId, FeeCategoryDto dto) {
        ownership.validate(schoolId);

        if (feeCategoryRepository.existsByNameAndSchoolId(dto.getName(), schoolId)) {
            throw new IllegalArgumentException("Fee category with name " + dto.getName() + " already exists");
        }
        FeeCategory entity = new FeeCategory();
        entity.setSchoolId(schoolId);
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setFrequency(dto.getFrequency());
        entity.setIsMandatory(dto.getIsMandatory());

        return mapToDto(feeCategoryRepository.save(entity));
    }

    @Transactional(readOnly = true)
    @Cacheable("fee-categories")
    public List<FeeCategoryDto> getAllCategories(UUID schoolId) {
        ownership.validate(schoolId);
        return feeCategoryRepository.findAll(FinanceSpecification.activeScoped())
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @CacheEvict(value = "fee-structures", allEntries = true)
    public FeeStructureDto createStructure(UUID schoolId, FeeStructureDto dto) {
        ownership.validate(schoolId);

        if (feeStructureRepository.existsByNameAndAcademicYearAndSchoolId(dto.getName(), dto.getAcademicYear(),
                schoolId)) {
            throw new IllegalArgumentException(
                    "Fee structure with name " + dto.getName() + " already exists for this year");
        }

        FeeStructure entity = new FeeStructure();
        entity.setSchoolId(schoolId);
        entity.setName(dto.getName());
        entity.setAcademicYear(dto.getAcademicYear());
        entity.setFrequency(dto.getFrequency());
        entity.setLateFeeAmount(dto.getLateFeeAmount());
        entity.setGracePeriodDays(dto.getGracePeriodDays());

        if (dto.getItems() != null) {
            List<FeeStructureItem> items = dto.getItems().stream().map(itemDto -> {
                FeeCategory category = feeCategoryRepository
                        .findOne(FinanceSpecification.activeById(itemDto.getCategoryId()))
                        .orElseThrow(
                                () -> new IllegalArgumentException("Invalid category ID: " + itemDto.getCategoryId()));

                FeeStructureItem item = new FeeStructureItem();
                item.setFeeStructure(entity);
                item.setFeeCategory(category);
                item.setAmount(itemDto.getAmount());
                return item;
            }).collect(Collectors.toList());
            entity.setItems(items);
        }

        return mapStructureToDto(feeStructureRepository.save(entity));
    }

    @Transactional(readOnly = true)
    @Cacheable("fee-structures")
    public List<FeeStructureDto> getAllStructures(UUID schoolId) {
        ownership.validate(schoolId);
        return feeStructureRepository.findAll(FinanceSpecification.activeScoped())
                .stream().map(this::mapStructureToDto).collect(Collectors.toList());
    }

    public void assignStructureToClass(Long structureId, Long classId) {
        FeeStructure structure = feeStructureRepository.findOne(FinanceSpecification.activeById(structureId))
                .orElseThrow(() -> new IllegalArgumentException("Structure not found"));

        SchoolClass schoolClass = schoolClassRepository.findOne(SchoolClassSpecification.activeById(classId))
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        LocalDate effectiveFrom = LocalDate.now();
        ClassFeeAssignment assignment = classFeeAssignmentRepository
                .findBySchoolIdAndStudentClassIdAndFeeStructureIdAndAcademicYearAndEffectiveFrom(
                        structure.getSchoolId(), classId, structureId, structure.getAcademicYear(), effectiveFrom)
                .orElseGet(() -> classFeeAssignmentRepository.save(ClassFeeAssignment.builder()
                        .schoolId(structure.getSchoolId())
                        .studentClass(schoolClass)
                        .feeStructure(structure)
                        .academicYear(structure.getAcademicYear())
                        .effectiveFrom(effectiveFrom)
                        .isActive(true)
                        .build()));

        List<Student> students = studentRepository.findAll(StudentSpecification.activeScoped())
                .stream().filter(s -> s.getStudentClass().getId().equals(classId)).toList();

        List<StudentFeeMap> newMaps = students.stream()
                .filter(student -> !studentFeeMapRepository.existsByStudentIdAndClassFeeAssignmentId(student.getId(),
                        assignment.getId()))
                .map(student -> {
                    StudentFeeMap map = new StudentFeeMap();
                    map.setStudent(student);
                    map.setFeeStructure(structure);
                    map.setSchoolId(structure.getSchoolId());
                    map.setClassFeeAssignment(assignment);
                    map.setIsActive(true);
                    return map;
                })
                .collect(Collectors.toList());

        if (!newMaps.isEmpty()) {
            studentFeeMapRepository.saveAll(newMaps);
        }

        auditLogService.logAction(null, "FEE_ASSIGNMENT", "CLASS", classId.toString(),
                "Assigned fee structure " + structure.getName() + " to class " + schoolClass.getName(), null, null,
                "COMPLETED");
    }

    @Transactional(readOnly = true)
    public StudentLedgerDto getStudentLedger(Long studentId) {
        Student student = studentRepository.findOne(StudentSpecification.activeById(studentId))
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<StudentFeeMap> feeMaps = studentFeeMapRepository.findAll(FinanceSpecification.activeScoped())
                .stream().filter(m -> m.getStudent().getId().equals(studentId) && m.getIsActive()).toList();

        List<PaymentTransaction> transactions = paymentTransactionRepository
                .findAll(FinanceSpecification.activeScoped())
                .stream().filter(t -> t.getStudent().getId().equals(studentId))
                .sorted((t1, t2) -> t2.getPaymentDate().compareTo(t1.getPaymentDate()))
                .toList();

        BigDecimal totalDues = BigDecimal.ZERO;
        BigDecimal totalLateFees = BigDecimal.ZERO;
        List<LedgerEntryDto> ledgerEntries = new ArrayList<>();

        for (StudentFeeMap map : feeMaps) {
            FeeStructure structure = map.getFeeStructure();
            BigDecimal baseAmount = structure.getItems().stream()
                    .map(FeeStructureItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discount = map.getDiscountAmount() != null ? map.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal netAmount = baseAmount.subtract(discount);

            BigDecimal lateFee = BigDecimal.ZERO;
            if (map.getCreatedAt() != null && structure.getGracePeriodDays() != null
                    && structure.getLateFeeAmount() != null) {
                Instant deadline = map.getCreatedAt().plus(structure.getGracePeriodDays(), ChronoUnit.DAYS);
                if (Instant.now().isAfter(deadline)) {
                    lateFee = structure.getLateFeeAmount();
                }
            }

            totalDues = totalDues.add(netAmount).add(lateFee);
            totalLateFees = totalLateFees.add(lateFee);

            LedgerEntryDto entry = new LedgerEntryDto();
            entry.setStudentFeeMapId(map.getId());
            entry.setStructureName(structure.getName());
            entry.setAcademicYear(structure.getAcademicYear());
            entry.setBaseAmount(baseAmount);
            entry.setDiscountAmount(discount);
            entry.setNetAmount(netAmount);

            ledgerEntries.add(entry);
        }

        BigDecimal totalPaid = transactions.stream()
                .filter(t -> "SUCCESS".equals(t.getStatus()))
                .map(PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingBalance = totalDues.subtract(totalPaid);

        StudentLedgerDto ledger = new StudentLedgerDto();
        ledger.setStudentId(studentId);
        ledger.setTotalDues(totalDues);
        ledger.setTotalPaid(totalPaid);
        ledger.setPendingBalance(pendingBalance);
        ledger.setLateFees(totalLateFees);
        ledger.setDues(ledgerEntries);
        ledger.setTransactions(transactions.stream().map(this::mapTransactionToDto).collect(Collectors.toList()));

        return ledger;
    }

    public PaymentTransactionDto recordPayment(PaymentRequestDto dto) {
        if (dto.getTransactionReference() != null && !dto.getTransactionReference().isEmpty()) {
            if (paymentTransactionRepository.existsByTransactionReference(dto.getTransactionReference())) {
                throw new IllegalArgumentException("Duplicate transaction reference: " + dto.getTransactionReference());
            }
        }

        Student student = studentRepository.findOne(StudentSpecification.activeById(dto.getStudentId()))
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        UUID schoolId = student.getSchoolId();

        StudentLedgerDto ledger = getStudentLedger(dto.getStudentId());

        if (dto.getAmount().compareTo(ledger.getPendingBalance()) > 0) {
            throw new IllegalArgumentException("Payment exceeds pending balance");
        }

        PaymentTransaction tx = new PaymentTransaction();
        tx.setStudent(student);
        tx.setSchoolId(schoolId);
        studentFeeMapRepository.findTopByStudentIdAndIsActiveTrueOrderByCreatedAtAsc(dto.getStudentId())
                .ifPresent(tx::setStudentFeeMap);

        if (dto.getCategoryId() != null) {
            feeCategoryRepository.findOne(FinanceSpecification.activeById(dto.getCategoryId()))
                    .ifPresent(tx::setFeeCategory);
        }
        if (dto.getForMonth() != null) {
            tx.setForMonth(dto.getForMonth());
        }

        tx.setAmount(dto.getAmount());
        tx.setPaymentMethod(dto.getPaymentMethod());
        tx.setTransactionReference(dto.getTransactionReference());
        tx.setStatus("SUCCESS");
        tx.setPaymentDate(LocalDateTime.now());

        PaymentTransaction saved = paymentTransactionRepository.save(tx);

        Receipt receipt = new Receipt();
        receipt.setTransaction(saved);
        receipt.setSchoolId(schoolId);
        receipt.setReceiptNumber("REC-" + saved.getId() + "-" + (System.currentTimeMillis() % 1000));
        receiptRepository.save(receipt);

        auditLogService.logAction(null, "PAYMENT_RECORDED", "TRANSACTION", String.valueOf(saved.getId()),
                "Payment of " + saved.getAmount() + " recorded for student " + student.getId(), null, null, "SUCCESS");

        return mapTransactionToDto(saved);
    }

    @Transactional(readOnly = true)
    public byte[] getReceiptPdf(String receiptNumber) {
        Receipt receipt = receiptRepository.findOne(FinanceSpecification.<Receipt>activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("receiptNumber"), receiptNumber)))
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));

        PaymentTransaction tx = receipt.getTransaction();
        Student student = tx.getStudent();

        Map<String, Object> data = new HashMap<>();
        data.put("receiptNumber", receipt.getReceiptNumber());
        data.put("date", tx.getPaymentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        data.put("studentName", student.getName());
        data.put("className", student.getStudentClass() != null ? student.getStudentClass().getName() : "N/A");
        data.put("amount", tx.getAmount());
        data.put("paymentMethod", tx.getPaymentMethod());
        data.put("transactionRef", tx.getTransactionReference());

        if (tx.getFeeCategory() != null) {
            data.put("feeCategory", tx.getFeeCategory().getName());
        }
        if (tx.getForMonth() != null) {
            data.put("forMonth", tx.getForMonth().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }

        return receiptService.generateReceiptPdf(data);
    }

    @CacheEvict(value = { "fee-categories", "fee-structures" }, allEntries = true)
    public FeeCategoryDto updateCategory(Long id, FeeCategoryDto dto) {
        FeeCategory category = feeCategoryRepository.findOne(FinanceSpecification.activeById(id))
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (!category.getName().equals(dto.getName())
                && feeCategoryRepository.existsByNameAndSchoolId(dto.getName(), category.getSchoolId())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setType(dto.getType());
        category.setFrequency(dto.getFrequency());
        category.setIsMandatory(dto.getIsMandatory());

        return mapToDto(feeCategoryRepository.save(category));
    }

    @CacheEvict(value = { "fee-categories", "fee-structures" }, allEntries = true)
    public void deleteCategory(Long id) {
        FeeCategory category = feeCategoryRepository.findOne(FinanceSpecification.activeById(id))
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (feeStructureItemRepository.existsByFeeCategoryId(id)) {
            throw new IllegalArgumentException("Cannot delete category used in fee structures");
        }

        feeCategoryRepository.delete(category);
    }

    public FeeStructureDto updateStructure(Long id, FeeStructureDto dto) {
        FeeStructure structure = feeStructureRepository.findOne(FinanceSpecification.activeById(id))
                .orElseThrow(() -> new IllegalArgumentException("Structure not found"));

        structure.setName(dto.getName());
        structure.setAcademicYear(dto.getAcademicYear());
        structure.setFrequency(dto.getFrequency());
        structure.setLateFeeAmount(dto.getLateFeeAmount());
        structure.setGracePeriodDays(dto.getGracePeriodDays());

        if (dto.getItems() != null) {
            structure.getItems().clear();
            List<FeeStructureItem> items = dto.getItems().stream().map(itemDto -> {
                FeeCategory category = feeCategoryRepository
                        .findOne(FinanceSpecification.activeById(itemDto.getCategoryId()))
                        .orElseThrow(
                                () -> new IllegalArgumentException("Invalid category: " + itemDto.getCategoryId()));

                FeeStructureItem item = new FeeStructureItem();
                item.setFeeStructure(structure);
                item.setFeeCategory(category);
                item.setAmount(itemDto.getAmount());
                return item;
            }).collect(Collectors.toList());

            structure.getItems().addAll(items);
        }

        return mapStructureToDto(feeStructureRepository.save(structure));
    }

    public void deleteStructure(Long id) {
        FeeStructure structure = feeStructureRepository.findOne(FinanceSpecification.activeById(id))
                .orElseThrow(() -> new IllegalArgumentException("Structure not found"));

        if (studentFeeMapRepository.existsByFeeStructureIdAndSchoolId(id, structure.getSchoolId())) {
            throw new IllegalArgumentException("Cannot delete fee structure assigned to students");
        }

        feeStructureRepository.delete(structure);
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getAllTransactions(UUID schoolId) {
        ownership.validate(schoolId);
        return paymentTransactionRepository.findAll(FinanceSpecification.activeScoped())
                .stream().map(this::mapTransactionToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getTransactionsByStudentId(Long studentId) {
        studentRepository.findOne(StudentSpecification.activeById(studentId))
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return paymentTransactionRepository.findAll(FinanceSpecification.activeScoped())
                .stream().filter(t -> t.getStudent().getId().equals(studentId))
                .sorted((t1, t2) -> t2.getPaymentDate().compareTo(t1.getPaymentDate()))
                .map(this::mapTransactionToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DefaulterDto> getDefaulters(UUID schoolId) {
        ownership.validate(schoolId);

        List<Student> students = studentRepository.findByStudentClass_SchoolIdAndDeletedFalse(schoolId);
        List<DefaulterDto> defaulters = new ArrayList<>();

        for (Student student : students) {
            try {
                StudentLedgerDto ledger = getStudentLedger(student.getId());
                if (ledger.getPendingBalance().compareTo(BigDecimal.ZERO) > 0) {
                    DefaulterDto dto = new DefaulterDto();
                    dto.setId(student.getId());
                    dto.setStudentName(student.getName());
                    dto.setStudentId(student.getRollNo() != null ? String.valueOf(student.getRollNo())
                            : String.valueOf(student.getId()));
                    dto.setGrade(student.getStudentClass() != null ? student.getStudentClass().getName() : "N/A");
                    dto.setAmountDue(ledger.getPendingBalance());
                    dto.setDaysOverdue(0);
                    defaulters.add(dto);
                }
            } catch (Exception e) {
                // Ignore errors for specific students
            }
        }
        return defaulters;
    }

    private FeeCategoryDto mapToDto(FeeCategory entity) {
        FeeCategoryDto dto = new FeeCategoryDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType());
        dto.setFrequency(entity.getFrequency());
        dto.setIsMandatory(entity.getIsMandatory());
        return dto;
    }

    private FeeStructureDto mapStructureToDto(FeeStructure entity) {
        FeeStructureDto dto = new FeeStructureDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setFrequency(entity.getFrequency());
        dto.setLateFeeAmount(entity.getLateFeeAmount());
        dto.setGracePeriodDays(entity.getGracePeriodDays());

        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream().map(item -> {
                FeeStructureItemDto itemDto = new FeeStructureItemDto();
                itemDto.setId(item.getId());
                itemDto.setCategoryId(item.getFeeCategory().getId());
                itemDto.setCategoryName(item.getFeeCategory().getName());
                itemDto.setAmount(item.getAmount());
                return itemDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private PaymentTransactionDto mapTransactionToDto(PaymentTransaction val) {
        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setId(val.getId());
        dto.setStudentId(val.getStudent().getId());
        dto.setAmount(val.getAmount());
        dto.setPaymentMethod(val.getPaymentMethod());
        dto.setTransactionReference(val.getTransactionReference());
        dto.setStatus(val.getStatus());
        dto.setPaymentDate(val.getPaymentDate());

        if (val.getFeeCategory() != null) {
            dto.setCategoryId(val.getFeeCategory().getId());
            dto.setCategoryName(val.getFeeCategory().getName());
        }
        if (val.getForMonth() != null) {
            dto.setForMonth(val.getForMonth());
        }

        return dto;
    }

    @Transactional
    public void generateInvoicesForSchool(UUID schoolId) {
        // Implementation for generating invoices
        log.info("Generating invoices for school {}", schoolId);
    }
}
