# Tasks: Reporting & Analytics

**Branch**: `005-reporting-analytics`

## Phase 1: Setup & Academic Analytics
- [x] T001 Create DTOs: `ReportCardDataDto`, `SubjectMarkDto`, `AttendanceSummaryDto` in `src/main/java/com/cm/sanchalak/dto/analytics/`
- [x] T002 Implement `AnalyticsService.getReportCardData` calculating marks and attendance
- [x] T003 Create `AnalyticsController` for `/api/analytics/report-card/{studentId}`

## Phase 2: Financial Analytics
- [x] T004 Create DTOs: `FinancialSummaryDto`, `CollectionTrendDto`
- [x] T005 Implement `AnalyticsService.getFinancialSummary` (Total Dues vs Collected)
- [x] T006 Implement `AnalyticsService.getCollectionTrend` (Daily/Monthly aggregation)
- [x] T007 Add financial endpoints to `AnalyticsController`

## Phase 3: Testing
- [x] T008 Integration Test for Report Card Data (Mock Marks/Attendance)
- [x] T009 Integration Test for Financial Stats (Mock Transactions)
