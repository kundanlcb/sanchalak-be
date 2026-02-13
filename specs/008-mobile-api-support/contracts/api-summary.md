# Mobile API Contracts

**Feature**: Mobile API Backend Support  
**Version**: v1.0  
**Base Path**: `/api/mobile/v1`

## API Contract Summary

Full OpenAPI 3.0 specification to be generated using Spring annotations. This document outlines the key endpoint contracts.

## Authentication Endpoints

### POST /api/mobile/v1/auth/request-otp
Request OTP for mobile login
- **Request**: `{ "mobileNumber": "string" }`
- **Response 200**: `{ "success": true, "message": "OTP sent", "expiresIn": 300 }`
- **Response 429**: Rate limit exceeded

### POST /api/mobile/v1/auth/verify-otp
Verify OTP and get tokens
- **Request**: `{ "mobileNumber": "string", "otp": "string" }`
- **Response 200**: `{ "accessToken": "string", "refreshToken": "string", "user": {...} }`
- **Response 401**: Invalid OTP

### POST /api/mobile/v1/auth/refresh
Refresh access token
- **Request**: `{ "refreshToken": "string" }`
- **Response 200**: `{ "accessToken": "string", "refreshToken": "string" }`

### POST /api/mobile/v1/auth/logout
Revoke refresh token
- **Headers**: Authorization: Bearer {token}
- **Response 200**: `{ "success": true }`

## User Context Endpoints

### GET /api/mobile/v1/me
Get current user profile
- **Headers**: Authorization: Bearer {token}
- **Response 200**: `{ "userId": number, "name": "string", "role": "STUDENT|PARENT", "studentId": number?, "parentId": number? }`

### GET /api/mobile/v1/me/students
Get linked students (parent only)
- **Headers**: Authorization: Bearer {token}
- **Response 200**: `[ { "studentId": number, "name": "string", "class": "string", "section": "string" } ]`

### GET /api/mobile/v1/me/home
Get dashboard data
- **Headers**: Authorization: Bearer {token}
- **Query**: `studentId` (required for parent)
- **Response 200**: `{ "attendance": {...}, "homeworkCount": number, "nextExam": {...}, "pendingFees": number, "recentNotices": [...] }`

## Attendance Endpoints (Wrappers)

### GET /api/mobile/v1/attendance/summary
- **Query**: `studentId` (optional, auto-resolved for student)
- **Response 200**: Mobile-formatted attendance summary

### GET /api/mobile/v1/attendance/history
- **Query**: `studentId`, `startDate`, `endDate`
- **Response 200**: Date-wise attendance records

## Homework Endpoints

### GET /api/mobile/v1/homework
- **Query**: `studentId`, `classId`, `date`
- **Response 200**: Homework list with submission status

### POST /api/mobile/v1/homework/{id}/submit
- **Request**: Multipart form with files
- **Response 201**: Homework submission created

### GET /api/mobile/v1/homework/{id}/submission
- **Response 200**: Submission details with file URLs

## Fees Endpoints (Wrappers)

### GET /api/mobile/v1/fees/ledger
- **Query**: `studentId`
- **Response 200**: Fee summary and breakdown

### POST /api/mobile/v1/fees/pay
- **Request**: Payment details with transactionId
- **Response 200**: Payment confirmation

### GET /api/mobile/v1/fees/receipt/{receiptId}
- **Response 200**: PDF receipt or receipt details

## Transport Tracking Endpoints

### GET /api/mobile/v1/transport/my-route
- **Headers**: Authorization: Bearer {token}
- **Response 200**: Assigned route details

### GET /api/mobile/v1/transport/live
- **Query**: `routeId`
- **Response 200**: `{ "latitude": number, "longitude": number, "speed": number, "lastUpdated": "timestamp", "stale": boolean }`

### GET /api/mobile/v1/transport/stops
- **Query**: `routeId`
- **Response 200**: Ordered stop list with ETA

### GET /api/mobile/v1/transport/events
- **Query**: `studentId`, `date`
- **Response 200**: Pickup/drop event history

## Notice Endpoints

### GET /api/mobile/v1/notices
- **Query**: `priority`, `startDate`, `endDate`
- **Response 200**: Notice list with read status

### GET /api/mobile/v1/notices/{id}
- **Response 200**: Notice details (marks as read)

## Notification Management

### POST /api/mobile/v1/notifications/register
- **Request**: `{ "token": "string", "platform": "android|ios", "deviceId": "string" }`
- **Response 200**: Token registered

### POST /api/mobile/v1/notifications/unregister
- **Request**: `{ "token": "string" }`
- **Response 200**: Token removed

## Standard Response Envelope

All endpoints return:
```json
{
  "success": boolean,
  "data": object | array | null,
  "error": {
    "code": "string",
    "message": "string",
    "details": object
  } | null,
  "meta": {
    "requestId": "string",
    "timestamp": "ISO8601",
    "pagination": {
      "page": number,
      "pageSize": number,
      "total": number
    } | null
  }
}
```

## Error Codes

- `AUTH_001`: Invalid credentials
- `AUTH_002`: OTP expired
- `AUTH_003`: Rate limit exceeded
- `AUTH_004`: Invalid refresh token
- `AUTHZ_001`: Unauthorized child access
- `AUTHZ_002`: Insufficient permissions
- `VAL_001`: Validation error
- `SYS_001`: Internal server error
- `DATA_001`: Resource not found

---

**Implementation Note**: Generate full OpenAPI 3.0 YAML using Springdoc annotations before controller implementation.
