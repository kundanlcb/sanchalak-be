# Karate Journey Coverage Report

**Project**: `sanchalak_be`  
**Date**: 2026-02-16  
**Status**: Journey suites created

## 1. Journey Files Added

- `src/test/resources/karate/journeys/J01_sanchalan_admin_onboarding.feature`
- `src/test/resources/karate/journeys/J02_sanchalan_web_ops_finance.feature`
- `src/test/resources/karate/journeys/J03_guru_teacher_daily_flow.feature`
- `src/test/resources/karate/journeys/J04_gurukul_mobile_core.feature`
- `src/test/resources/karate/journeys/J05_cross_product_orchestration.feature`

## 2. Coverage by Product

### `sanchalan-admin`
Covered in:
- `J01_sanchalan_admin_onboarding.feature`
- `J05_cross_product_orchestration.feature`

Covered capabilities:
- platform auth
- school create/update/status transition
- academic bootstrap (year/class/subject)
- operations config
- bootstrap school admin
- subscription plan create/assign
- import job submission
- onboarding readiness checks

### `sanchalan` (web)
Covered in:
- `J02_sanchalan_web_ops_finance.feature`
- `J05_cross_product_orchestration.feature`

Covered capabilities:
- admin auth
- subject/teacher/student setup
- attendance marking + class sheet
- notice publishing + read feed
- finance category/structure/assign
- ledger read + payment recording

### `guru` (teacher app)
Covered in:
- `J03_guru_teacher_daily_flow.feature`
- `J05_cross_product_orchestration.feature`

Covered capabilities:
- teacher auth
- routine read
- attendance bulk mark
- homework create + list
- notice create + list

### `gurukul` (student/parent app)
Covered in:
- `J04_gurukul_mobile_core.feature`
- `J05_cross_product_orchestration.feature`

Covered capabilities:
- student auth
- `/api/me` profile surface
- notification register/unregister
- attendance summary endpoint
- homework listing
- notice feed
- finance ledger validation path
- calendar failure-path handling for unlinked student profile

## 3. Shared Infrastructure Added

- `src/test/resources/karate-config.js`
- `src/test/resources/karate/common/session.feature`
- `src/test/resources/karate/common/platform-auth.feature`
- updated `src/test/resources/karate/common/auth.feature`

## 4. Runner and Build Integration

Already present from setup:
- `src/test/java/com/cm/sanchalak/karate/KarateSmokeRunner.java`
- `src/test/java/com/cm/sanchalak/karate/KarateRegressionRunner.java`
- `src/test/java/com/cm/sanchalak/karate/KarateJourneyRunner.java`
- Gradle tasks:
  - `karateSmoke`
  - `karateRegression`
  - `karateJourney`

## 5. Current Limitation Observed

- Full personalized student/parent `/api/me/*` happy paths need explicit `student.userId` and parent linkage provisioning via API or test seed utility.
- Current journeys therefore validate both happy-path and guardrail/error-path behavior where linkage setup is not exposed through public APIs.

## 6. Suggested Next Step

- Add a dedicated test-only seed endpoint or test fixture loader for creating linked student-parent-user graphs.
- Then upgrade `J04` to full mobile-personalized happy-path (dashboard, timetable, results, fees, transport with real linkage).
