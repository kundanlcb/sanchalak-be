# Quickstart: Finance Module

## Prerequisites

*   **Java**: JDK 25
*   **Database**: PostgreSQL running on port 5432
*   **Backend**: Reference `application.yml` for connection details.

## Running the Application

1.  **Start DB**: Ensure Postgres is running.
2.  **Migrate**: Run `./gradlew flywayMigrate` to apply `V5__finance_module.sql`.
3.  **Boot**: Run `./gradlew bootRun`.

## Testing the API (Curl)

### 1. Create a Fee Category (Tuition)
```bash
curl -X POST http://localhost:8080/api/finance/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tuition Fee",
    "description": "Base academic fee",
    "isMandatory": true
  }'
```

### 2. Create a Fee Structure
```bash
curl -X POST http://localhost:8080/api/finance/structures \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Class 10 Standard",
    "academicYear": "2026-27",
    "frequency": "MONTHLY",
    "items": [
      { "categoryId": 1, "amount": 5000.00 }
    ]
  }'
```

### 3. Assign to a Student `1`
```bash
curl -X POST http://localhost:8080/api/finance/structures/1/assign \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1
  }'
```

### 4. Check Dues
```bash
curl http://localhost:8080/api/finance/students/1/ledger
```

### 5. Pay Fee
```bash
curl -X POST http://localhost:8080/api/finance/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "amount": 5000.00,
    "paymentMethod": "CASH",
    "gatewayTransactionId": "MANUAL-001"
  }'
```
