# Quickstart: Attendance Service

## Prerequisites

- **Branch**: `003-attendance-service`
- **Database**: PostgreSQL (or H2 for dev)
- **Dependencies**: `002-academic-management` (must be merged or present)

## Running the Feature

1.  **Start Database**:
    ```bash
    docker-compose up -d db
    ```
2.  **Run Migrations**:
    - Flyway `V4__attendance_schema.sql` will run automatically on boot.
3.  **Start Backend**:
    ```bash
    ./gradlew bootRun
    ```

## Testing

### Integration Tests
Run the dedicated test suite for attendance:
```bash
./gradlew test --tests com.cm.sanchalak.AttendanceIntegrationTest
```

### Manual Verification (cURL)

**1. Create Attendance (Bulk)**
```bash
curl -X POST http://localhost:8080/api/attendance/bulk \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "classId": 1,
    "date": "2026-02-12",
    "markedBy": "teacher_1",
    "attendances": [
      {"studentId": 1, "status": "PRESENT"},
      {"studentId": 2, "status": "ABSENT", "remarks": "Sick"}
    ]
  }'
```

**2. Get Class Sheet**
```bash
curl -X GET http://localhost:8080/api/attendance/class/1/date/2026-02-12 \
  -H "Authorization: Bearer <TOKEN>"
```
