# Data Model Changes

## New Fields

### Student
- `deleted` (Boolean, Default: false): Soft delete flag.
- `guardianName` (String): (If not existing, ensure it is added/used).
- `guardianMobile` (String): (If not existing).

## Relationships for Analytics
- `ExamSchedule` JOIN `ClassSubject` on (class_id, subject_id) to link `Teacher`.

## Integrity Constraints
- `Teacher` DELETE: Block if exists in `ClassRoutine`.
- `Subject` DELETE: Block if exists in `ClassRoutine`.
- `Student` DELETE: Block HARD DELETE if exists in `StudentFeeMap`. ALLOW Soft Delete.
