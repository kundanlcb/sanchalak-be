# Quickstart: Academic Management

## 1. Verify Migrations
Application startup should apply `V3__academic_tables.sql`.

```sql
SELECT * FROM exam_terms;
SELECT * FROM subjects;
```

## 2. Seed Academic Data (Admin)

Create Term:
```bash
curl -X POST http://localhost:8080/api/academic/terms \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Mid-Term 2026", "startDate": "2026-06-01", "endDate": "2026-06-15"}'
```

Create Subject:
```bash
curl -X POST http://localhost:8080/api/academic/subjects \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Mathematics", "code": "MATH"}'
```

Link to Class:
```bash
curl -X POST http://localhost:8080/api/academic/classes/1/subjects \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"subjectId": 1}'
```

## 3. Teacher Workflow

Schedule Exam (Admin/Teacher):
```bash
curl -X POST http://localhost:8080/api/academic/schedules \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"examTermId": 1, "classId": 1, "subjectId": 1, "maxMarks": 50, "examDate": "2026-06-02"}'
```

Enter Marks:
```bash
curl -X POST http://localhost:8080/api/academic/marks \
  -H "Authorization: Bearer $TEACHER_TOKEN" \
  -d '{"studentId": 1, "examScheduleId": 1, "marksObtained": 45.5}'
```

## 4. Verification

Check Report Card:
```bash
curl -X GET http://localhost:8080/api/academic/reports/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```
