# Implementation Plan: Reporting & Analytics

**Branch**: `005-reporting-analytics`  
**Feature**: Reporting & Analytics (Report Cards, Financial Dashboards)  
**Spec**: `specs/005-reporting-analytics/spec.md`  

## Summary

Implement the analytics layer to aggregate data for School Reporting. This includes generating the JSON data structure required for Client-Side PDF Report Card generation (Academic Core) and providing statistical endpoints for the Financial Dashboard (Financial Admin). The implementation relies on creating Read-Only DTOs that aggregate data from `StudentMarks`, `AttendanceRecord`, `PaymentTransaction`, and `FeeStructure`.

## Technical Context

**Language/Version**: Java 25 (Spring Boot 4.0.2)
**Persistence**: PostgreSQL (JPA/Hibernate)
**Key Dependencies**: Existing Entities (`Student`, `StudentMarks`, `AttendanceRecord`, `PaymentTransaction`).
**Security**: Endpoints protected by Role-Based Access Control (`ADMIN`, `PRINCIPAL`, `TEACHER`).
**Performance**: 
- Report Card aggregation involves complex joins; explicit JPQL or efficient Stream processing required.
- Financial Charts require aggregation (SUM/COUNT) over time periods; Database-level grouping preferred over in-memory.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Separation of Concerns**: Analytics logic resides in `AnalyticsService` and `AnalyticsController`, separate from transactional services.
- [x] **DTO Usage**: Usage of specific DTOs (`ReportCardDataDto`, `FinancialSummaryDto`) prevents leaking Entity internals.
- [x] **Security**: Data access restricted by `@PreAuthorize`.

## Phases

### Phase 1: Setup & Academic Analytics
**Goal**: Enable Client-Side PDF Generation for Report Cards.
- **DTOs**: Define nested structure (`ReportCardDataDto` -> `Academics`, `Attendance`, `Profile`).
- **Service**: Implement `getReportCardData(studentId, termId)`.
  - Fetch `Student` details.
  - Fetch `ExamTerm` dates.
  - Query `AttendanceRecord` count within Term dates.
  - Query `StudentMarks` joined with `ExamSchedule` for the Term.
  - Compute Totals, Percentage, and Assign Grades (Simple static grading scale initially).
- **Controller**: Expose `/api/analytics/report-card/{studentId}`.

### Phase 2: Financial Analytics
**Goal**: Power the Financial Dashboard.
- **DTOs**: `FinancialSummaryDto` (Total Dues, Collected, Outstanding), `CollectionTrendDto` (Date, Amount).
- **Service**: Implement `getFinancialSummary()` and `getCollectionTrend(period)`.
  - Reuse `FinanceService` ledger logic for summary totals.
  - Implement Custom Repository Query for Daily/Monthly collection grouping.
- **Controller**: Expose `/api/analytics/finance/*`.

### Phase 3: Testing & Integration
- **Integration Tests**: Verify `AnalyticsController` returns correct JSON structure.
- **Performance Check**: Ensure Report Card aggregation < 200ms.

## Artifacts

```text
specs/005-reporting-analytics/
├── plan.md              # This file
├── spec.md              # Requirements
└── tasks.md             # Actionable Tasks
```