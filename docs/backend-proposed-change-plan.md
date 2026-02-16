# Backend Proposed Change Plan (`sanchalak_be`)

**Project**: `sanchalak_be`  
**Date**: 2026-02-16  
**Audience**: Backend engineering team  
**Goal**: Align backend to support stable onboarding and operations across `sanchalan-admin`, `sanchalan`, `guru`, and `gurukul`.

## 1. Scope

This document proposes concrete backend changes only (no frontend changes), based on current implemented code and observed gaps.

## 2. Current State (Summary)

Implemented:
- Platform APIs exist for school onboarding, academic setup, subscription assignment, support impersonation, and import upload trigger.
- Core school APIs exist for academics, attendance, notices, finance, transport, homework, and profile (`/api/me/*`).
- Karate journey suite exists and covers cross-product flows.

Major gaps:
- Incomplete tenant isolation and school scoping in core services.
- Platform import pipeline is scaffold-only (no robust validation/preview/status/errors/retry).
- Support unlock is placeholder.
- Platform DB model is partly Hibernate-managed (`ddl-auto: update`) instead of fully Flyway-managed.
- Role model mismatch (`Staff` usage in clients vs missing backend role constants).
- Mobile profile/dashboard paths still include partial placeholder behavior.
- Karate dependency version is `1.4.1`, target is `1.5.2`.

## 3. Proposed Changes (Prioritized)

## 3.1 P0 - Must Do First (Stability + Data Safety)

1. **Enforce tenant/school scoping in core domain services**
- Add mandatory `schoolId` resolution and filtering for student, teacher, academic, and finance reads/writes.
- Block cross-school access in service layer even if controller is called with valid auth.
- Primary targets:
  - `src/main/java/com/cm/sanchalak/service/StudentService.java`
  - `src/main/java/com/cm/sanchalak/service/TeacherService.java`
  - `src/main/java/com/cm/sanchalak/service/AcademicService.java`
  - `src/main/java/com/cm/sanchalak/service/FinanceService.java`
  - related repositories (`findBy...AndSchoolId`)

2. **Replace hardcoded school resolution in finance config**
- Remove static UUID placeholder from `FinanceConfigController#getSchoolId()`.
- Resolve school context from authenticated user -> school mapping (`school_users`) or explicit school context header strategy.
- Target:
  - `src/main/java/com/cm/sanchalak/controller/FinanceConfigController.java`

3. **Move platform persistence to Flyway control**
- Create migration scripts for platform tables currently auto-created by JPA:
  - `schools`
  - `platform_users`
  - `subscription_plans`
  - `school_subscriptions`
  - `import_jobs`
  - `school_operation_configs`
  - platform audit/support tables introduced below
- Set non-dev profile to `ddl-auto: validate`.
- Targets:
  - `src/main/resources/db/migration/V21__...sql` onward
  - `src/main/resources/application.yaml` (profile-aware config)

4. **Implement support unlock operation fully**
- Add actual account lock fields to `users` if missing (`account_non_locked`, `failed_attempts`, `locked_at`).
- Implement unlock mutation + audit event.
- Targets:
  - `src/main/java/com/cm/sanchalak/platform/support/SupportService.java`
  - user entity/repository + new migration

5. **Fix platform import base behavior**
- Add status endpoint to poll job:
  - `GET /api/platform/v1/schools/{schoolId}/imports/{jobId}`
- Persist row-level errors and summary fields correctly.
- Targets:
  - `src/main/java/com/cm/sanchalak/platform/web/ImportController.java`
  - `src/main/java/com/cm/sanchalak/platform/importing/ImportService.java`
  - new import row result entity/table

## 3.2 P1 - Required for Complete Onboarding Workflow

1. **Import pipeline v2 (Preview -> Commit -> Retry)**
- New endpoints:
  - `POST /api/platform/v1/schools/{schoolId}/imports/preview`
  - `POST /api/platform/v1/schools/{schoolId}/imports/commit`
  - `GET /api/platform/v1/import-jobs/{jobId}`
  - `GET /api/platform/v1/import-jobs/{jobId}/errors`
  - `POST /api/platform/v1/import-jobs/{jobId}/retry`
- Parse CSV/XLSX, validate duplicates, class/section references, parent linkage, required fields.
- Support idempotency key on commit.

2. **Platform RBAC hardening**
- Enforce role checks on all platform controllers.
- Suggested coarse permissions:
  - `OWNER`: full
  - `OPS`: schools, academic structure, import
  - `FINANCE`: plans/subscriptions/finance ops
  - `SUPPORT`: unlock, impersonation
- Targets:
  - `src/main/java/com/cm/sanchalak/platform/web/*.java`
  - `src/main/java/com/cm/sanchalak/platform/auth/PlatformRole.java`
  - method-level `@PreAuthorize` rules

3. **Subscription lifecycle hardening**
- Add plan change history and status transition logic (`ACTIVE`, `PAYMENT_DUE`, `RESTRICTED`, etc.).
- Add scheduler for overdue transitions and enforcement hooks.
- Targets:
  - `src/main/java/com/cm/sanchalak/platform/subscription/SubscriptionService.java`
  - new history/audit tables

4. **Onboarding completion contract enforcement**
- Prevent `ACTIVE` finalization unless required readiness checklist is complete.
- Readiness checks should include:
  - profile
  - academic year + class baseline
  - school admin linked
  - active subscription

## 3.3 P2 - Product Maturity + Operational Excellence

1. **Profile/dashboard placeholder removal**
- Replace mock dashboard aggregation with real repository-backed aggregation.
- Target:
  - `src/main/java/com/cm/sanchalak/service/DashboardAggregationService.java`

2. **Role model alignment**
- Add missing role constants if required by product (`ROLE_STAFF` and/or `ROLE_OFFICE_STAFF`), with migration + seed updates.
- Target:
  - `src/main/java/com/cm/sanchalak/entity/RoleName.java`
  - seed/migration scripts

3. **Transport migration cleanup**
- Resolve duplicate migration family (`V13` and `V16`) into single canonical transport schema path.
- Add forward-only corrective migration; do not rewrite applied migrations.

4. **Observability + audit completeness**
- Mandatory audit logs for platform privileged actions:
  - school status change
  - admin bootstrap
  - import commit/retry
  - subscription assign/change
  - unlock and impersonation

## 4. API Changes (Backend Deliverables)

## 4.1 New/Updated Platform APIs

1. Import:
- `POST /api/platform/v1/schools/{schoolId}/imports` (keep, but return richer DTO)
- `GET /api/platform/v1/schools/{schoolId}/imports/{jobId}` (new)
- `POST /api/platform/v1/schools/{schoolId}/imports/preview` (new)
- `POST /api/platform/v1/schools/{schoolId}/imports/commit` (new)
- `GET /api/platform/v1/import-jobs/{jobId}/errors` (new)
- `POST /api/platform/v1/import-jobs/{jobId}/retry` (new)

2. Support:
- `POST /api/platform/v1/support/unlock/{userId}` (implement real mutation)
- `POST /api/platform/v1/support/impersonate` (keep, add TTL/scope + audit)

3. Subscription:
- `POST /api/platform/v1/subscriptions/assign/{schoolId}` (keep)
- add optional effective date and reason fields
- add history endpoint:
  - `GET /api/platform/v1/subscriptions/history/{schoolId}` (new)

## 4.2 Core API Contract Tightening

- Add school-scoped authorization checks to all core CRUD surfaces.
- Standardize error shape with domain codes (authorization, validation, conflict, linkage errors).

## 5. Schema and Data Changes

1. Add/normalize columns:
- `users`: lock-related fields for support unlock.
- `students`, `teachers`, `subjects`, `fee_categories`, `fee_structures`: explicit `school_id` constraints/indexes where missing.

2. New platform tables:
- `import_row_results`
- `support_actions`
- `subscription_history`
- `platform_audit_events` (or reuse `audit_logs` with platform action typing)

3. Indexes:
- composite indexes on `(school_id, <frequent-filter-field>)` for student/teacher/finance paths.

4. Seeds:
- seed all required roles (including staff variants if adopted).
- keep platform bootstrap admin but force password rotation in non-local env.

## 6. Testing and Regression Updates

1. Upgrade Karate dependency:
- `build.gradle`:
  - from `com.intuit.karate:karate-junit5:1.4.1`
  - to `com.intuit.karate:karate-junit5:1.5.2`

2. Expand regression coverage:
- Add module-level regression features for:
  - platform imports (preview/commit/retry)
  - support unlock
  - tenant isolation negative tests
  - subscription transition scheduler

3. Keep journey tests and upgrade assertions:
- ensure `J04_gurukul_mobile_core.feature` validates real happy-path once linkage setup is exposed.

## 7. Execution Plan

1. **Sprint 1 (P0)**
- tenant/school scoping
- finance schoolId fix
- unlock implementation
- import status endpoint
- Flyway platform baseline migrations

2. **Sprint 2 (P1)**
- import preview/commit/retry
- platform RBAC hardening
- subscription lifecycle enhancements

3. **Sprint 3 (P2)**
- dashboard aggregation real data
- role alignment
- transport migration cleanup
- observability/audit hardening

## 8. Definition of Done

- School onboarding can be completed end-to-end from platform APIs without manual DB operations.
- Cross-school data leakage is blocked by service-layer enforcement.
- Import flow supports preview, commit, retry, and row-level error reporting.
- Support unlock and impersonation are functional and audited.
- Production profile runs with Flyway-managed schema (`ddl-auto: validate`).
- Karate uses `1.5.2` and regression suite gates backend changes.
