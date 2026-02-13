# Backend Specification Gap Analysis

**Date**: 13 Feb 2026
**Purpose**: Compare Frontend (FE) requirements with Backend (BE) implementation status.

## Analysis Summary

The backend implementation (`sanchalak_be`) has good coverage for Core Academics (`002`), Attendance (`003`), Finance (`004`), and Reporting (`005`). However, distinct gaps exist for the newly introduced Frontend requirements in `004-school-ops` (Dashboard, Teachers, Routine) and `006-full-crud-ops` (Comprehensive Management).

## Missing Specifications & Coverage Plan

### 1. School Operations (Timetable/Routine)
- **FE Requirement**: `004-school-ops` demands a Weekly Routine/Timetable manager (Class x Day x Period).
- **BE Status**: Missing `Routine` entity and related logic.
- **Action**: Created `specs/006-school-operations-dashboard/spec.md` to define `ClassRoutine` entity and API.

### 2. Enhanced Teacher Management
- **FE Requirement**: `004-school-ops` & `006-full-crud-ops` require detailed Teacher profiles (Qualification, Subjects) and full CRUD.
- **BE Status**: `Teacher` entity is minimal. Controller lacks full CRUD.
- **Action**: Included in `specs/006-school-operations-dashboard/spec.md`.

### 3. Dashboard Analytics Enhancements
- **FE Requirement**: `004-school-ops` Dashboard includes:
  - Gender Distribution Chart.
  - Teacher Performance Metrics.
  - Recent Activity Feed.
- **BE Status**: `AnalyticsController` covers Report Cards and Finance, but lacks these specific operational metrics.
- **Action**: Included in `specs/006-school-operations-dashboard/spec.md`.

### 4. Full CRUD Operations
- **FE Requirement**: `006-full-crud-ops` requires Update/Delete capabilities for:
  - `Class` & `Subject`
  - `FeeStructure` & `FeeCategory`
  - `Student` (Archive/Soft Delete)
- **BE Status**: Existing controllers focus on Creation and Reading. Update/Delete logic is missing or partial.
- **Action**: Included in `specs/006-school-operations-dashboard/spec.md`.

## Conclusion
A new backend feature branch `006-school-operations-dashboard` is required to implement these missing specifications. The spec file has been created at `specs/006-school-operations-dashboard/spec.md`.
