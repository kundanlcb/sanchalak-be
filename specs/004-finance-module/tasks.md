# Tasks: Finance Module (Backend)

**Branch**: `004-finance-module`
**Feature**: Finance Module (Fee Management)
**Status**: Planning

## Phase 1: Setup
**Goal**: Initialize dependencies and database schema.

- [x] T001 Add `openhtmltopdf` and `thymeleaf` dependencies to `build.gradle`
- [x] T002 Create Flyway migration `src/main/resources/db/migration/V5__finance_schema.sql` with tables from data model

## Phase 2: Foundational (Entities & Repos)
**Goal**: Create persistent layer matching the schema.

- [x] T003 [P] Create `FeeCategory` entity in `src/main/java/com/cm/sanchalak/entity/FeeCategory.java`
- [x] T004 [P] Create `FeeStructure` and `FeeStructureItem` entities in `src/main/java/com/cm/sanchalak/entity/`
- [x] T005 [P] Create `StudentFeeMap` entity in `src/main/java/com/cm/sanchalak/entity/StudentFeeMap.java`
- [x] T006 [P] Create `PaymentTransaction` and `Receipt` entities in `src/main/java/com/cm/sanchalak/entity/`
- [x] T007 [P] Create all Repositories (`FeeCategoryRepository`, `FeeStructureRepository`, `StudentFeeMapRepository`, `PaymentTransactionRepository`, `ReceiptRepository`) in `src/main/java/com/cm/sanchalak/repository/`

## Phase 3: User Story 1 - Fee Structure Configuration
**Goal**: Allow admin to define fees.

- [x] T008 [US1] Create `FeeCategoryDto` and `FeeStructureDto` in `src/main/java/com/cm/sanchalak/dto/finance/`
- [x] T009 [US1] Implement `FinanceService.createCategory` and `getAllCategories`
- [x] T010 [US1] Implement `FinanceService.createStructure` with items
- [x] T011 [US1] Implement `FinanceService.assignStructure` to Student class
- [x] T012 [US1] Create `FinanceConfigController` with endpoints `/api/finance/categories` and `/api/finance/structures`

## Phase 4: User Story 2 - Transaction Processing & Ledger
**Goal**: Calculate dues and accept payments.

- [x] T013 [US2] Implement `StudentLedgerDto` and `PaymentRequestDto` in `src/main/java/com/cm/sanchalak/dto/finance/`
- [x] T014 [US2] Implement `FinanceService.getStudentLedger` with Late Fee & Due calculation logic
- [x] T015 [US2] Implement `FinanceService.recordPayment` with Idempotency check and balance validation
- [x] T016 [US2] Create `FinanceOperationsController` with endpoints `/api/finance/students/{id}/ledger` and `/transactions`

## Phase 5: User Story 3 - Receipt Generation
**Goal**: Generate professional PDF receipts.

- [x] T017 [US3] Create `src/main/resources/templates/receipt.html` Thymeleaf template
- [x] T018 [US3] Implement `ReceiptService` to render HTML and convert to PDF using `openhtmltopdf`
- [x] T019 [US3] Integrate `ReceiptService` into `FinanceService.recordPayment` to generate receipt on success
- [x] T020 [US3] Implement `FinanceService.getReceipt` to retrieve stored receipt details
- [x] T021 [US3] Add endpoint `/api/finance/receipts/{receiptNo}` to Controller

## Phase 6: Polish
**Goal**: Verification and Integration Testing.

- [x] T022 Create `FinanceServiceTest` for Unit Testing Ledger Logic (Mockito)
- [x] T023 Create `FinanceIntegrationTest` for End-to-End flow (Config -> Pay -> Receipt)

## Dependencies
US1 (Config) -> US2 (Ledger) -> US3 (Receipts)
Entities (Phase 2) are prerequisites for all User Stories.
