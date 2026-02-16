# Universal Data Seeding and CRUD Plan (`sanchalak_be`)

**Project**: `sanchalak_be`  
**Date**: 2026-02-16  
**Focus**: Universal master data readiness + CRUD support.

## 1. Objective

Make the system production-ready by introducing:
- reliable, idempotent universal seed data
- clear CRUD APIs for masters needed by onboarding and daily operations
- strict control over which masters are editable vs system-managed

## 2. Universal Data Scope

Universal data means platform-wide reference data that every school depends on.

## 2.1 Global System Masters (Seeded, mostly locked)

1. **Academic structure templates**
- class templates: `Nursery`, `LKG`, `UKG`, `1`..`12`
- section templates: `A`..`H`
- academic term templates: `Unit Test`, `Half Yearly`, `Final`

2. **School profile masters**
- boards: `CBSE`, `ICSE`, `STATE_BOARD`, `IB`, `IGCSE`
- mediums: `ENGLISH`, `HINDI`, `BILINGUAL`, others
- school status values: `DRAFT`, `ACTIVE`, `PAYMENT_DUE`, `RESTRICTED`, `SUSPENDED`, `ARCHIVED`

3. **People/profile masters**
- genders
- blood groups
- parent relationship types (`MOTHER`, `FATHER`, `GUARDIAN`, etc.)
- occupation list (base set, extensible)

4. **Attendance and academics masters**
- attendance statuses: `PRESENT`, `ABSENT`, `LATE`, `HALF_DAY`, `LEAVE`
- grading bands (default grading scale)
- mark entry statuses (`DRAFT`, `PUBLISHED`, `LOCKED`)

5. **Finance masters**
- fee frequencies: `MONTHLY`, `QUARTERLY`, `HALF_YEARLY`, `ANNUAL`
- payment methods: `CASH`, `UPI`, `CARD`, `NET_BANKING`, `CHEQUE`
- transaction statuses: `SUCCESS`, `PENDING`, `FAILED`, `REFUNDED`
- default fee category templates: `TUITION`, `TRANSPORT`, `EXAM`, `LIBRARY`, `LAB`, `MISC`

6. **Notice/communication masters**
- notice priorities: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- target roles: `ALL`, `STUDENT`, `PARENT`, `TEACHER`, `STAFF`
- notification platforms: `FCM`, `APNS`, `WEB`

7. **Transport masters**
- vehicle types (`BUS`, `VAN`, etc.)
- route types (`PICKUP`, `DROP`, `BOTH`)
- trip statuses (`SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`)
- transport event types (`PICKED`, `DROPPED`, `MISSED`, `ALERT`)

8. **Document masters**
- student document types (`BIRTH_CERTIFICATE`, `AADHAR`, `TRANSFER_CERTIFICATE`, etc.)

## 2.2 School-Scoped Masters (Seed default + CRUD editable)

1. Academic year
2. Classes (from templates)
3. Sections (from templates)
4. Subjects (base defaults + school additions)
5. Fee categories and fee structures
6. School operation config toggles

## 3. Data Governance Rules

1. **System locked masters**
- enums/status/value sets critical to logic should be non-deletable from API.
- allow only `active/inactive` toggle where needed.

2. **Editable masters**
- school-facing configurable masters (subjects, fee categories, grading scale) can be CRUD, scoped by `school_id`.

3. **No hard delete for referenced masters**
- use soft delete (`is_active=false`) where records are referenced.

4. **Dependency checks on delete**
- reject delete if a master is used in students, fees, attendance, notices, etc.

5. **Versioning and ordering**
- masters should support display order and audit version metadata.

## 4. Proposed Data Model

## 4.1 Option A (Recommended): Generic Master Registry

1. `master_domains`
- `id`, `code`, `name`, `scope` (`SYSTEM`/`SCHOOL`), `is_editable`, `is_deletable`

2. `master_values`
- `id`, `domain_id`, `code`, `label`, `metadata_json`, `sort_order`, `is_active`, `seed_version`, `school_id (nullable)`

3. `master_value_audit`
- `id`, `domain_code`, `value_code`, `action`, `actor_id`, `old_value_json`, `new_value_json`, `created_at`

Benefits:
- one CRUD framework for all master data
- easy to seed and patch by `domain+code`
- no schema churn for each new lookup list

## 4.2 Option B: Dedicated Tables per Master

Use dedicated tables (`boards`, `mediums`, `attendance_statuses`, etc.).

Use only if strict SQL constraints per domain are mandatory. For current speed + maintainability, Option A is better.

## 5. Seeding Strategy

## 5.1 Seed Sources

1. `db/migration` bootstrap inserts for minimum startup-safe values
2. app-level seeder for large/default catalogs from YAML/JSON resources

## 5.2 Idempotency Rules

1. Upsert by unique key: `(domain_code, code, school_id)`
2. Never duplicate if same seed re-runs
3. Store `seed_version` and `seeded_at`

## 5.3 Environment Policy

1. `local/test`: full default catalog
2. `staging/prod`: controlled catalog + explicit seed version gate
3. no manual DB edits; all through migration/seeder/API

## 5.4 Roll-forward Only

- any master correction via next migration/seed version
- no destructive rewrite of historical seed rows

## 6. CRUD API Contract (Master Data)

Base path proposal: `/api/platform/v1/masters`

1. `GET /domains`
- list domain definitions with governance flags

2. `GET /domains/{domainCode}/values`
- query params: `schoolId`, `activeOnly`, `search`, `page`, `size`

3. `POST /domains/{domainCode}/values`
- create value (allowed only if domain editable)

4. `PUT /domains/{domainCode}/values/{valueId}`
- update label, metadata, sort order, active flag

5. `DELETE /domains/{domainCode}/values/{valueId}`
- soft delete or block if non-deletable/referenced

6. `POST /domains/{domainCode}/seed-defaults`
- seed defaults to a school for school-scoped domains

7. `POST /reseed`
- platform admin only; runs seed engine for specified `seedVersion`

## 7. School Onboarding Seed Flow

When school is created:

1. Create school row
2. Seed school masters from global templates:
- default class set
- default section set
- baseline subjects
- baseline fee categories
- default grading scale
3. Mark onboarding step `mastersSeeded=true`
4. Expose status in onboarding API

## 8. Backend Changes Required

1. New migration set:
- `V21__create_master_registry.sql`
- `V22__seed_system_master_domains.sql`
- `V23__seed_system_master_values.sql`

2. New modules:
- `master` package (`entity`, `repository`, `service`, `web`)

3. New seeder:
- `UniversalMasterSeeder` (idempotent startup seeding)

4. Refactor existing hardcoded/enum usage where needed:
- replace static lists in service/controller with master lookup where dynamic behavior is required

5. Add authorization:
- only platform roles with `MASTER_WRITE` can mutate masters

## 9. Validation and CRUD Rules

1. Unique code per domain per scope
2. Reserved codes cannot be updated/deleted
3. Reject delete when referenced
4. Return standardized error codes:
- `MASTER_DOMAIN_NOT_EDITABLE`
- `MASTER_VALUE_IN_USE`
- `MASTER_CODE_CONFLICT`
- `MASTER_RESERVED_CODE`

## 10. Testing Plan

1. Unit tests:
- seeder idempotency
- dependency validation on delete
- reserved code guardrails

2. Integration tests:
- CRUD lifecycle per editable domain
- school default seeding flow
- onboarding status reflects seeded masters

3. Karate tests (`1.5.2` target):
- master list/read/create/update/delete flows
- negative scenarios for locked domains and in-use values
- onboarding journey check that school defaults are seeded

## 11. Initial Deliverable Slice (Recommended)

## Sprint A
- master registry tables + seed engine
- domains: boards, mediums, class templates, section templates, fee frequencies, payment methods
- read APIs

## Sprint B
- write APIs for editable domains
- dependency checks + audit
- school default seeding trigger at onboarding

## Sprint C
- migrate remaining reference sets
- full regression coverage and rollout

## 12. Definition of Done

1. Fresh environment can bootstrap all required universal masters with one migration + seeder run.
2. Editable master domains support full CRUD with validation and audit.
3. School onboarding automatically seeds required school-scoped defaults.
4. No production logic depends on undocumented hardcoded lists.
