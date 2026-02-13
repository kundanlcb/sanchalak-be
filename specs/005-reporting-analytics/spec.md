# Feature Specification: Reporting & Analytics

**Feature Branch**: `005-reporting-analytics`  
**Created**: 13 Feb 2026  
**Status**: Draft  

## User Scenarios & Testing

### User Story 1 - Academic Report Card Data (Priority: P1)

As a school administrator, I want to fetch comprehensive academic and attendance data for a student so that I can generate their Report Card (PDF) on the client side.

**Why this priority**: Core functionality for end-of-term operations. The frontend `002-academic-core` relies on this data for its "No Backend Rendering" PDF strategy.

**Independent Test**: Call endpoint with Student ID and Term ID, verify JSON contains Profile, Marks, Attendance Summary, and Grading details.

**Acceptance Scenarios**:

1. **Given** a student with marks in Math and English, **When** I request report card data, **Then** the response includes the student's personal info, class details, marks for both subjects, and attendance percentage for the term.
2. **Given** a student with no attendance records, **When** I request data, **Then** attendance is returned as 0% (graceful handling).

---

### User Story 2 - Financial Analytics Dashboard (Priority: P2)

As an administrator, I want to see aggregated financial metrics so that I can understand the school's cash flow.

**Why this priority**: Required for the `003-financial-admin` and `005-connect-real-data` frontend dashboards.

**Independent Test**: Create several payments, call the stats endpoint, verify the "Total Collected" matches sum of payments.

**Acceptance Scenarios**:

1. **Given** 3 payments of 1000 each today, **When** I fetch financial stats, **Then** "Total Collection" shows 3000.
2. **Given** payments spread across months, **When** I fetch chart data, **Then** the response lists collection grouped by month/date.

---

### User Story 3 - Class Academic Performance (Priority: P3)

As a teacher, I want to see how my class performed in an exam term (averages, pass/fail rates).

**Why this priority**: Useful for `004-school-ops` or general dashboard widgets.

**Acceptance Scenarios**:

1. **Given** a class with 10 students, **When** I fetch term performance, **Then** I get the average percentage and top performers.

---

## Requirements

### Functional Requirements

- **FR-001**: System MUST provide an endpoint to aggregate **Marks**, **Attendance**, and **Fee Status** (optional) for a single student.
- **FR-002**: System MUST calculate **Total Marks**, **Percentage**, and **Effective Grade** for the report card data.
- **FR-003**: System MUST provide **Financial Summary** (Total Dues, Total Collected, Pending) based on the `FinanceService` ledger logic.
- **FR-004**: System MUST provide **Time-Series Data** (Daily/Monthly) for fee collections for charting.
- **FR-005**: All analytics endpoints MUST be protected (ADMIN/PRINCIPAL/OFFICE_STAFF roles).

### Key Entities (Read-Only Aggregation)

- **StudentMarks**: Source of academic data.
- **AttendanceRecord**: Source of attendance stats.
- **PaymentTransaction**: Source of financial charts.
- **FeeStructure**: Source of expected revenue.

## API Design

### 1. Academic Reports
```http
GET /api/analytics/report-card/{studentId}?termId={termId}
```
**Response**:
```json
{
  "student": { "name": "...", "rollNumber": "..." },
  "term": { "name": "..." },
  "attendance": { "totalDays": 100, "presentDays": 85, "percentage": 85.0 },
  "academics": [
    { "subject": "Math", "score": 85, "maxMarks": 100, "grade": "A" }
  ],
  "result": { "totalScore": 85, "percentage": 85.0, "rank": 5 }
}
```

### 2. Financial Analytics
```http
GET /api/analytics/finance/summary
```
**Response**:
```json
{
  "totalRevenue": 500000,
  "totalCollected": 350000,
  "totalOutstanding": 150000
}
```

```http
GET /api/analytics/finance/collection-trend?period=MONTHLY
```

- **[Entity 2]**: [What it represents, relationships to other entities]

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: [Measurable metric, e.g., "Users can complete account creation in under 2 minutes"]
- **SC-002**: [Measurable metric, e.g., "System handles 1000 concurrent users without degradation"]
- **SC-003**: [User satisfaction metric, e.g., "90% of users successfully complete primary task on first attempt"]
- **SC-004**: [Business metric, e.g., "Reduce support tickets related to [X] by 50%"]
