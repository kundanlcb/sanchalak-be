# Karate Regression Strategy for `sanchalak_be`

**Project**: `sanchalak_be`  
**Created**: 2026-02-16  
**Status**: Proposed (implementation-ready)

## 1. Objective
Introduce a structured Karate regression framework so backend changes do not break existing behavior. The framework must validate:
- core API contracts
- module-level behavior
- full real-user journeys across multiple features

## 2. Why Karate Here
Karate fits this backend because it gives:
- readable `.feature` files for business flows
- strong API chaining support (auth token -> next calls)
- good data-driven testing for role/validation scenarios
- easy CI execution and HTML reports

## 3. Testing Scope Model
We will run 3 layers:

1. **Smoke (`@smoke`)**
- Fast critical health checks after every PR
- Login/auth, key CRUD, basic authorization

2. **Regression (`@regression`)**
- Broader module coverage
- Attendance, academics, finance, notices, transport, mobile APIs

3. **Journey (`@journey`)**
- End-to-end real user flows combining modules
- Example: authenticate -> setup class -> add student -> mark attendance -> fee payment -> dashboard validation

## 4. Proposed Folder Structure

```text
src/test/
├── java/com/cm/sanchalak/karate/
│   ├── KarateSmokeRunner.java
│   ├── KarateRegressionRunner.java
│   └── KarateJourneyRunner.java
└── resources/
    ├── karate-config.js
    ├── karate/
    │   ├── common/
    │   │   ├── auth.feature
    │   │   ├── setup-data.feature
    │   │   ├── cleanup-data.feature
    │   │   └── assertions.feature
    │   ├── fixtures/
    │   │   ├── users/
    │   │   ├── academics/
    │   │   ├── attendance/
    │   │   └── finance/
    │   ├── modules/
    │   │   ├── auth/
    │   │   ├── academics/
    │   │   ├── attendance/
    │   │   ├── finance/
    │   │   ├── notices/
    │   │   ├── transport/
    │   │   └── mobile/
    │   └── journeys/
    │       ├── J01_school_bootstrap_to_first_attendance.feature
    │       ├── J02_parent_otp_to_homework_and_fees.feature
    │       ├── J03_teacher_day_flow.feature
    │       └── J04_finance_collection_cycle.feature
    └── sql/
        ├── seed-minimal.sql
        └── cleanup.sql
```

## 5. Tagging Strategy
Use tags to control pipeline execution:
- `@smoke` critical checks
- `@regression` full module tests
- `@journey` cross-module end-to-end tests
- `@auth`, `@attendance`, `@finance`, `@mobile` domain filters
- `@negative` validation/error scenarios
- `@wip` excluded from CI gates

## 6. Real User Journey Suites (Must-Have)

### J01: School Bootstrap to First Attendance
Flow:
1. Admin login
2. Create class + subject + teacher
3. Create/import student
4. Assign routine
5. Mark attendance in bulk
6. Validate attendance summary/dashboard

### J02: Parent OTP to Homework and Fees
Flow:
1. Request OTP + verify OTP
2. Fetch linked students
3. Fetch homework list
4. Submit homework (where allowed)
5. Fetch fee ledger
6. Record payment and validate updated pending balance

### J03: Teacher Daily Flow
Flow:
1. Teacher auth
2. Get assigned class/routine
3. Mark attendance
4. Create homework
5. Validate class-level attendance stats

### J04: Finance Collection Cycle
Flow:
1. Configure fee category + structure
2. Assign structure to class/student
3. Read student ledger
4. Record payment transaction
5. Validate receipt and collection summary metrics

## 7. Test Data Strategy
- Each scenario uses unique IDs (timestamp/UUID suffix) to avoid collisions.
- Shared reusable setup via `call read('classpath:karate/common/setup-data.feature')`.
- Prefer API-driven setup over DB direct inserts for behavior realism.
- DB cleanup hooks only where strictly needed for deterministic runs.
- Keep fixtures small and composable.

## 8. Environment Strategy
Use `karate-config.js` with environments:
- `local`: `http://localhost:8082`
- `ci`: ephemeral test container URL
- `staging`: secure staging host (read-only safe test data)

Config values:
- `baseUrl`
- test credentials
- timeout/retry defaults
- optional feature toggles

## 9. Build and Runner Integration (Gradle)

## 9.1 Dependencies
Add:
- `testImplementation 'com.intuit.karate:karate-junit5:1.4.1'`

## 9.2 Runner Classes
Create dedicated JUnit 5 runners selecting paths + tags.

## 9.3 Gradle Tasks
Introduce separate tasks:
- `karateSmoke`
- `karateRegression`
- `karateJourney`

And keep current `test` for unit/service/controller tests.

Recommended gate:
- PR: `test + karateSmoke`
- nightly: `test + karateRegression`
- pre-release: `test + karateRegression + karateJourney`

## 10. CI/CD Execution Plan

### Pull Request Pipeline
- run unit tests
- run `@smoke`
- fail PR on any regression

### Nightly Pipeline
- run full `@regression`
- publish Karate HTML reports
- alert on new failures

### Release Pipeline
- run smoke + regression + journeys
- block release if any journey fails

## 11. Reporting and Debuggability
- Store Karate HTML reports as CI artifacts.
- Add request/response logging for failed scenarios.
- Include correlation/request IDs in assertions where possible.
- Create a "known failures" quarantine only with explicit expiry date.

## 12. Rollout Plan

### Phase 1 (Week 1)
- Add Karate dependencies and base config
- Add smoke auth + attendance + finance basic checks

### Phase 2 (Week 2)
- Add module regression suites for academics, notices, mobile OTP
- Add CI nightly job

### Phase 3 (Week 3)
- Add 4 journey suites (`J01-J04`)
- Add release gating

### Phase 4 (Week 4)
- Stabilize flaky tests
- Add coverage dashboard (feature-wise pass/fail trend)

## 13. Governance Rules
- Every new endpoint must include at least one Karate happy-path test.
- Every bug fix must include a regression scenario reproducing the bug.
- Breaking API changes require feature updates in same PR.
- `@wip` tests cannot be part of release gate.

## 14. Definition of Done
Karate rollout is complete when:
- smoke runs on every PR and blocks merges on failure
- nightly full regression is stable
- journey suites cover critical user experience paths
- backend changes consistently trigger regression safety net
