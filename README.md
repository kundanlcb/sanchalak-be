# Sanchalak Backend

This repository contains the backend API for Sanchalak School Management System.

## Mobile API Support (Phase 8)

The backend exposes a dedicated API for the Mobile App (Student/Parent).

### Project Setup for Mobile

1. **Prerequisites**:
   - Java 25 (Amazon Corretto recommended)
   - Gradle 9.3+
   - MySQL 8.0+

2. **Configuration**:
   - Update `src/main/resources/application.yaml` (or `application.properties`) with:
     - JWT Secrets (`app.jwtSecret`)
     - OTP Settings (`app.otp.*`)
     - Firebase Credentials (`app.fcm.credentials-path`)
     - Helper services (S3/Azure)

3. **Running the Application**:
   ```bash
   ./gradlew bootRun
   ```

4. **API Documentation**:
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/v3/api-docs

### Authentication Flow (Mobile)

1. **Request OTP**:
   `POST /api/auth/otp/request`
   Body: `{"mobileNumber": "9876543210"}`
   Response: 200 OK (OTP sent)

2. **Verify OTP**:
   `POST /api/auth/otp/verify`
   Body: `{"mobileNumber": "9876543210", "otp": "123456", "deviceId": "..."}`
   Response: `{ "accessToken": "...", "refreshToken": "...", "user": {...} }`

3. **Refresh Token**:
   `POST /api/auth/refresh`
   Body: `{"refreshToken": "..."}`
   Response: New tokens.

### Key Features (Mobile)

- **Student Login**: View profile, attendance summary, homework.
- **Parent Login**: Link multiple children, view their data.
- **Transport Tracking**: Live bus location, stop ETAs.
- **Push Notifications**: Firebase Cloud Messaging integration.

### Testing

Run unit and integration tests:
```bash
./gradlew test
```

Specific mobile tests:
```bash
./gradlew test --tests com.cm.sanchalak.integration.OtpAuthenticationFlowTest
./gradlew test --tests com.cm.sanchalak.controller.MobileAuthControllerTest
```
