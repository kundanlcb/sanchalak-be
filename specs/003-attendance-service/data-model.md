# Data Model: Attendance Service

**Feature**: 003-attendance-service

## ER Diagram

```mermaid
erDiagram
    AttendanceRecord {
        Long id PK
        Long student_id FK
        Long class_id FK
        LocalDate date
        String status
        String remarks
        String marked_by
        LocalDateTime marked_date
        String modified_by
        LocalDateTime modified_date
        Boolean is_modified
    }

    Student ||--o{ AttendanceRecord : "has"
    Class ||--o{ AttendanceRecord : "logs"
```

## Schema Definitions

### Table: `attendance_records`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, Auto-inc | Unique ID |
| `student_id` | BIGINT | FK -> students.id, Not Null | Link to Student |
| `class_id` | BIGINT | FK -> classes.id, Not Null | Link to Class (Snapshot) |
| `date` | DATE | Not Null | Attendance Date |
| `status` | VARCHAR(20) | Not Null | Enum: PRESENT, ABSENT, LATE, EXCUSED, HOLIDAY |
| `remarks` | VARCHAR(255) | Nullable | Optional notes |
| `marked_by` | VARCHAR(100) | Not Null | User ID (audit) |
| `marked_date` | TIMESTAMP | Not Null | Audit |
| `modified_by` | VARCHAR(100) | Nullable | Audit |
| `modified_date` | TIMESTAMP | Nullable | Audit |
| `is_modified` | BOOLEAN | Default FALSE | Modification flag |

**Indexes**:
- Unique Index: `(student_id, date)` to prevent duplicates.
- Index: `(class_id, date)` for class sheet queries.
- Index: `(student_id)` for student history queries.

## Entities

### `AttendanceRecord`
- Extends `BaseEntity` (if available) or implements standard audit fields.
- `@Table(name = "attendance_records")`
- `@Entity`
