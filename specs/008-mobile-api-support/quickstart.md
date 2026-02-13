# Mobile API Quickstart Guide

**Feature**: 008-mobile-api-support  
**Target**: Local development environment setup for mobile API backend

---

## Prerequisites

- **Java 25 LTS** (JDK installed)
- **MySQL 8.0+** running locally or Docker
- **Gradle 8.x** (wrapper included)
- **Git** for branch management
- **Postman/curl** for API testing
- **Android Studio/Xcode** (optional, for mobile app testing)

---

## Step 1: Environment Setup

### 1.1 Clone & Checkout Feature Branch

```bash
cd /path/to/sanchalak_be
git checkout 008-mobile-api-support
```

### 1.2 Configure Database

Create MySQL database:
```sql
CREATE DATABASE sanchalak_mobile_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sanchalak_dev'@'localhost' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON sanchalak_mobile_dev.* TO 'sanchalak_dev'@'localhost';
FLUSH PRIVILEGES;
```

### 1.3 Application Configuration

Create `src/main/resources/application-dev.properties`:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/sanchalak_mobile_dev
spring.datasource.username=sanchalak_dev
spring.datasource.password=dev_password

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# JWT Settings
jwt.secret=dev-secret-key-change-in-production-min-256-bits
jwt.access-token-expiry=900000
jwt.refresh-token-expiry=2592000000

# OTP Settings
otp.expiry-seconds=300
otp.rate-limit-requests=3
otp.rate-limit-window-seconds=900
otp.encryption-key=AES256KeyChangeInProduction32

# AWS S3 (use LocalStack for dev)
aws.s3.bucket=sanchalak-dev-uploads
aws.s3.region=us-east-1
aws.s3.presigned-url-expiry-minutes=5

# Firebase Cloud Messaging
fcm.credentials-path=classpath:firebase-adminsdk-dev.json
fcm.enabled=true

# Async Executor
async.executor.core-pool-size=5
async.executor.max-pool-size=10
async.executor.queue-capacity=100

# Caching
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=3600s

# Logging
logging.level.com.cm.sanchalak=DEBUG
logging.level.org.springframework.security=DEBUG
```

---

## Step 2: Run Flyway Migrations

Migrations will run automatically on startup. Verify:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Check logs for:
```
Successfully applied 8 migrations to schema `sanchalak_mobile_dev`
Migrations: V8 through V15
```

---

## Step 3: Seed Test Data

### 3.1 Create Test Parent User

```sql
-- Insert parent user
INSERT INTO users (username, password, role, mobile_number, email, created_at)
VALUES ('parent_test', '$2a$10$hashed_password', 'ROLE_PARENT', '9876543210', 'parent@test.com', NOW());

-- Get parent user ID
SET @parent_user_id = LAST_INSERT_ID();

-- Insert parent record
INSERT INTO parents (user_id, first_name, last_name, mobile_number, created_at)
VALUES (@parent_user_id, 'Test', 'Parent', '9876543210', NOW());

SET @parent_id = LAST_INSERT_ID();

-- Link to existing student (assume student_id=1 exists)
INSERT INTO parent_student_links (parent_id, student_id, relationship_type, is_primary, created_at)
VALUES (@parent_id, 1, 'FATHER', true, NOW());
```

### 3.2 Create Test Student User

```sql
-- Update existing student with user account
INSERT INTO users (username, password, role, mobile_number, created_at)
VALUES ('student_test', '$2a$10$hashed_password', 'ROLE_STUDENT', '9998887770', NOW());

SET @student_user_id = LAST_INSERT_ID();

-- Link student to user
UPDATE students SET user_id = @student_user_id WHERE id = 1;
```

### 3.3 Create Test Transport Route

```sql
-- Insert vehicle
INSERT INTO vehicles (vehicle_number, vehicle_type, capacity, driver_name, driver_mobile, status)
VALUES ('DL01AB1234', 'BUS', 40, 'Test Driver', '9988776655', 'ACTIVE');

SET @vehicle_id = LAST_INSERT_ID();

-- Insert route
INSERT INTO routes (route_name, route_code, vehicle_id, start_time, end_time, route_type, status)
VALUES ('Test Route', 'TR-001', @vehicle_id, '07:00:00', '09:00:00', 'PICKUP', 'ACTIVE');

SET @route_id = LAST_INSERT_ID();

-- Insert stop
INSERT INTO stops (route_id, stop_name, stop_order, latitude, longitude, estimated_arrival)
VALUES (@route_id, 'Test Stop 1', 1, 28.6139, 77.2090, '07:30:00');

SET @stop_id = LAST_INSERT_ID();

-- Assign student to route
INSERT INTO student_transport_assignments (student_id, route_id, stop_id, assigned_date, status)
VALUES (1, @route_id, @stop_id, CURDATE(), 'ACTIVE');
```

---

## Step 4: Test API Endpoints

### 4.1 Request OTP

```bash
curl -X POST http://localhost:8080/api/mobile/v1/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{"mobileNumber": "9876543210"}'
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "message": "OTP sent successfully",
    "expiresIn": 300
  },
  "meta": {
    "requestId": "uuid",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

**Get OTP from DB** (dev only):
```sql
SELECT otp_code FROM otp_verifications 
WHERE mobile_number = '9876543210' 
ORDER BY created_at DESC LIMIT 1;
```

### 4.2 Verify OTP

```bash
curl -X POST http://localhost:8080/api/mobile/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"mobileNumber": "9876543210", "otp": "123456"}'
```

**Save tokens** from response.

### 4.3 Get User Profile

```bash
curl -X GET http://localhost:8080/api/mobile/v1/me \
  -H "Authorization: Bearer {ACCESS_TOKEN}"
```

### 4.4 Get Linked Students (Parent)

```bash
curl -X GET http://localhost:8080/api/mobile/v1/me/students \
  -H "Authorization: Bearer {ACCESS_TOKEN}"
```

### 4.5 Get Dashboard

```bash
curl -X GET "http://localhost:8080/api/mobile/v1/me/home?studentId=1" \
  -H "Authorization: Bearer {ACCESS_TOKEN}"
```

### 4.6 Track Transport

```bash
curl -X GET "http://localhost:8080/api/mobile/v1/transport/live?routeId=1" \
  -H "Authorization: Bearer {ACCESS_TOKEN}"
```

---

## Step 5: Firebase FCM Setup (Optional)

### 5.1 Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create project: `sanchalak-dev`
3. Add Android/iOS app
4. Download `google-services.json` / `GoogleService-Info.plist`

### 5.2 Generate Service Account Key

1. Project Settings → Service Accounts
2. Generate new private key
3. Save as `src/main/resources/firebase-adminsdk-dev.json`

### 5.3 Test Push Notification

```bash
curl -X POST http://localhost:8080/api/mobile/v1/notifications/register \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "fcm_device_token_from_mobile_app",
    "platform": "android",
    "deviceId": "test-device-001"
  }'
```

---

## Step 6: File Storage Setup (AWS S3 or Azure - Choose One)

### Option A: AWS S3 with LocalStack (Development)

#### 6.1 Run LocalStack

```bash
docker run -d \
  --name localstack-s3 \
  -p 4566:4566 \
  -e SERVICES=s3 \
  localstack/localstack
```

#### 6.2 Create Bucket

```bash
aws --endpoint-url=http://localhost:4566 s3 mb s3://sanchalak-dev-uploads
```

#### 6.3 Update application-dev.properties

```properties
# File Storage Provider
file-storage.provider=s3

# AWS S3 Configuration
file-storage.s3.endpoint=http://localhost:4566
file-storage.s3.bucket-name=sanchalak-dev-uploads
file-storage.s3.region=us-east-1
file-storage.s3.access-key=test
file-storage.s3.secret-key=test
file-storage.s3.path-style-access=true

# Upload Settings
file-storage.upload-url-expiry-minutes=5
file-storage.download-url-expiry-minutes=15
file-storage.max-file-size-mb=10
```

#### 6.4 Test Upload

```bash
# Get presigned URL
curl -X POST http://localhost:8080/api/mobile/v1/homework/1/submit \
  -H "Authorization: Bearer {TOKEN}" \
  -F "file=@test.pdf"
```

### Option B: Azure Blob Storage with Azurite (Development)

#### 6.1 Run Azurite

```bash
docker run -d \
  --name azurite \
  -p 10000:10000 \
  mcr.microsoft.com/azure-storage/azurite \
  azurite-blob --blobHost 0.0.0.0
```

#### 6.2 Create Container

```bash
az storage container create \
  --name sanchalak-dev-uploads \
  --connection-string "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;"
```

#### 6.3 Update application-dev.properties

```properties
# File Storage Provider
file-storage.provider=azure

# Azure Blob Storage Configuration
file-storage.azure.connection-string=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;
file-storage.azure.container-name=sanchalak-dev-uploads

# Upload Settings
file-storage.upload-url-expiry-minutes=5
file-storage.download-url-expiry-minutes=15
file-storage.max-file-size-mb=10
```

#### 6.4 Test Upload

```bash
# Get SAS URL
curl -X POST http://localhost:8080/api/mobile/v1/homework/1/submit \
  -H "Authorization: Bearer {TOKEN}" \
  -F "file=@test.pdf"
```

### Switching Between Providers

**To switch from S3 to Azure** (or vice versa), simply change one line:

```properties
# From: file-storage.provider=s3
# To:   file-storage.provider=azure
```

Then restart the application. No code changes required!

See [file-storage-abstraction.md](file-storage-abstraction.md) for detailed architecture.

---

## Step 7: Run Tests

### 7.1 Unit Tests

```bash
./gradlew test
```

### 7.2 Integration Tests

```bash
./gradlew integrationTest
```

### 7.3 API Contract Tests

```bash
./gradlew contractTest
```

---

## Common Issues & Troubleshooting

### Issue: Flyway Migration Failed

**Solution**: Drop database and recreate
```sql
DROP DATABASE sanchalak_mobile_dev;
CREATE DATABASE sanchalak_mobile_dev;
```

### Issue: JWT Token Invalid

**Check**:
- Token expiry (15 minutes for access token)
- JWT secret matches in properties
- Authorization header format: `Bearer {token}`

### Issue: Parent Can't Access Student Data

**Check**:
- Parent-student linkage exists in `parent_student_links`
- `is_active = true` and `effective_date <= NOW()`
- Cache may need refresh (wait 1 hour or restart server)

### Issue: OTP Not Sent

**Check**:
- Rate limit (3 requests per 15 minutes)
- Mobile number format validation
- Database connection for OTP storage

### Issue: Transport Live Location Returns Stale

**Check**:
- Location pings exist in last 2 minutes
- Trip is ACTIVE status
- GPS coordinates valid (not 0.0, 0.0)

### Issue: FCM Push Not Delivered

**Check**:
- `firebase-adminsdk-dev.json` exists and valid
- FCM token registered correctly
- Device token not expired (re-register from mobile app)
- Firebase project has Cloud Messaging enabled

---

## Mobile App Testing Setup

### Android (React Native)

1. Update `.env`:
```
API_BASE_URL=http://10.0.2.2:8080/api/mobile/v1
```
(10.0.2.2 is host machine from Android emulator)

2. Run:
```bash
npm start
# Press 'a' for Android
```

### iOS (React Native)

1. Update `.env`:
```
API_BASE_URL=http://localhost:8080/api/mobile/v1
```

2. Run:
```bash
npm start
# Press 'i' for iOS
```

---

## Next Steps

1. **Implement Controllers**: Start with `AuthController` (POST /auth/request-otp, /auth/verify-otp)
2. **Write Integration Tests**: Cover OTP flow, parent authorization, transport tracking
3. **Generate OpenAPI Spec**: Run `./gradlew generateOpenApiDocs` after annotation
4. **Load Test**: Use JMeter/Gatling for 1000 concurrent users
5. **Deploy Staging**: Follow deployment guide in `/docs/deployment.md`

---

**Questions?** Check `/specs/008-mobile-api-support/plan.md` for detailed implementation plan.
