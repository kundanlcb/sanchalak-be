# Backend Capabilities Required for `sanchalan-admin`

**Project**: `sanchalak_be`  
**Created**: 2026-02-15  
**Status**: Draft v1

## 1. Goal
Enable `sanchalan-admin` (internal FE) with secure platform-level backend capabilities for:
- school onboarding and lifecycle control
- student bulk import from Excel/CSV
- subscription and billing lifecycle management
- support operations (impersonation/unlock/retry)
- immutable audit and compliance reporting

## 2. Current Gap Summary
Existing APIs are mainly school-user oriented (`/api/academics`, `/api/finance`, etc.). Missing platform-oriented capabilities:
- platform RBAC model for internal team
- school tenant lifecycle APIs
- robust async import orchestration (preview, mapping, row-level outcomes)
- subscription enforcement engine tied to school access state
- centralized support operations and immutable platform audit events

## 3. Required Capability Areas

### 3.1 Platform Auth and RBAC
- Add platform-level roles and permissions separate from school roles.
- Include step-up auth support for high-risk operations.
- Enforce permission checks in all `/api/platform/v1/*` endpoints.

### 3.2 School Lifecycle Management
- Create and manage school records with strict uniqueness constraints.
- Bootstrap first school admin account.
- Manage lifecycle states:
  - `DRAFT`
  - `ACTIVE`
  - `PAYMENT_DUE`
  - `RESTRICTED`
  - `SUSPENDED`
  - `ARCHIVED`
- Persist state transition history with actor and reason.

### 3.3 Student Bulk Import Engine
- Support file upload for `.csv` and `.xlsx`.
- Parse + validate + deduplicate with preview step before commit.
- Commit via asynchronous jobs (queue + worker).
- Persist row-level results (`success`, `error_code`, message, normalized payload).
- Provide error report export and retry support.
- Idempotency key support to avoid duplicate imports.

### 3.4 Subscription and Billing Lifecycle
- Plan catalog CRUD (price, cycle, limits, entitlements).
- Assign/change school subscription with effective date and proration metadata.
- Invoice generation and payment recording (manual/gateway).
- Scheduler for overdue + grace evaluation and state transitions.
- Publish entitlement changes to access-control evaluation.

### 3.5 Support Operations
- Account unlock and admin reset triggers.
- Import retry and webhook replay.
- Impersonation session start/end with strict timeout and scope controls.
- Mandatory reason capture for high-impact support actions.

### 3.6 Audit and Compliance
- Append-only audit events for all privileged actions (success and failure).
- Include actor, action, target, reason, IP/device, request ID, and diff payload.
- Provide filtered retrieval and CSV export APIs.
- Compliance retention defaults to 7 years (configurable).

## 4. API Surface to Add (`/api/platform/v1`)

### Auth
- `POST /auth/login`
- `POST /auth/step-up/request`
- `POST /auth/step-up/verify`
- `POST /auth/logout`

### Schools
- `POST /schools`
- `GET /schools`
- `GET /schools/{schoolId}`
- `PATCH /schools/{schoolId}`
- `POST /schools/{schoolId}/status-transition`
- `POST /schools/{schoolId}/bootstrap-admin/reset`

### Imports
- `POST /schools/{schoolId}/imports/students/upload`
- `POST /schools/{schoolId}/imports/students/preview`
- `POST /schools/{schoolId}/imports/students/commit`
- `GET /schools/{schoolId}/imports/students/jobs`
- `GET /import-jobs/{jobId}`
- `GET /import-jobs/{jobId}/errors/export`
- `POST /import-jobs/{jobId}/retry`

### Subscription and Billing
- `POST /subscription-plans`
- `GET /subscription-plans`
- `PATCH /subscription-plans/{planId}`
- `POST /schools/{schoolId}/subscriptions/assign`
- `POST /schools/{schoolId}/subscriptions/change`
- `GET /schools/{schoolId}/subscriptions/history`
- `POST /invoices`
- `GET /invoices`
- `POST /invoices/{invoiceId}/payments`
- `POST /webhooks/payments/reconcile`

### Support and Audit
- `POST /support/impersonation/start`
- `POST /support/impersonation/end`
- `POST /support/accounts/{userId}/unlock`
- `POST /support/webhooks/{eventId}/replay`
- `GET /audit-events`
- `POST /audit-events/export`

## 5. Data Model Additions
- `platform_users`
- `platform_roles`
- `platform_role_permissions`
- `tenants`
- `schools`
- `school_status_history`
- `import_templates`
- `import_jobs`
- `import_row_results`
- `subscription_plans`
- `school_subscriptions`
- `invoices`
- `invoice_payments`
- `support_actions`
- `impersonation_sessions`
- `audit_events` (append-only)

### Critical Constraints
- Unique keys:
  - `tenants.tenant_code`
  - `schools.school_code`
  - `schools.registration_number`
- Per-school uniqueness:
  - `students.admission_no`
- All support/audit records must retain actor + request ID references.

## 6. Security and Isolation Requirements
- Enforce tenant isolation at repository/service layer by default.
- Platform-wide reads require explicit permission and reason logging.
- Step-up auth required for suspension, downgrades, impersonation.
- Idempotency for import commit and payment reconciliation operations.
- Rate limits for platform auth, import creation, and webhook replay.

## 7. Performance and Reliability Targets
- Non-bulk platform API p95: < 500ms.
- Import preview (10k rows): <= 15s.
- Import commit throughput: >= 2,000 rows/min/worker.
- School access-state enforcement latency after billing update: <= 2 min.
- Audit ingestion: 100% event capture for privileged actions.

## 8. Implementation Plan

### Phase 0: Foundation
- Platform RBAC model and shared permission middleware
- Base schema migrations for platform auth + audit

### Phase 1: School Lifecycle
- School CRUD + state transition engine + onboarding bootstrap

### Phase 2: Import Engine
- Upload/preview/commit APIs + queue workers + row result store + export/retry

### Phase 3: Subscription and Billing
- Plan catalog + subscription flows + invoice/payment APIs + enforcement scheduler

### Phase 4: Support and Audit
- Impersonation + unlock/replay operations + audit query/export APIs

### Phase 5: Hardening and Rollout
- Security tests, load tests, observability dashboards, pilot rollout, GA

## 9. Testing Requirements
- Unit tests:
  - state transition validator
  - import validator/dedupe logic
  - billing transition rules
  - authorization guards
- Integration tests:
  - school onboarding E2E
  - import preview/commit/retry E2E
  - invoice->payment->status transition E2E
  - impersonation lifecycle E2E
- Security tests:
  - tenant escape attempts
  - privilege escalation checks
  - audit immutability checks

## 10. Definition of Done
- `sanchalan-admin` can complete onboarding/import/subscription/support flows using only platform APIs.
- No manual DB intervention required for normal operations.
- 100% privileged actions recorded in audit events with actor and reason.
- Billing and import flows meet performance targets in staging before production rollout.
