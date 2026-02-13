# Phase 0: Research & Technology Decisions

**Feature**: Mobile API Backend Support  
**Date**: 2026-02-13  
**Purpose**: Resolve technical unknowns and establish implementation patterns before design/coding

## Research Areas

### 1. OTP Generation and Verification in Spring Boot

**Decision**: Use `SecureRandom` for OTP generation, encrypt OTPs at rest, implement rate limiting

**Rationale**:
- **OTP Generation**: `java.security.SecureRandom` provides cryptographically strong random numbers. Generate 6-digit numeric OTPs (000000-999999) for user-friendliness while maintaining 1-million combinations (sufficient for 5-minute expiry).
- **Storage**: Store encrypted OTPs in `otp_verification` table with expiry timestamp. Encrypt using AES-256 before persisting (Spring Crypto utilities).
- **Verification**: Constant-time comparison to prevent timing attacks. Mark OTP as `isUsed=true` after successful verification to prevent replay.
- **Rate Limiting**: Use Spring's `@RateLimiter` (Bucket4j or custom implementation) - max 3 OTP requests per mobile number per 15 minutes, max 5 verification attempts before temp lock.

**Alternatives Considered**:
- **Plaintext OTP storage**: Rejected - security risk if database compromised
- **TOTP (Time-based OTP)**: Rejected - requires shared secret distribution, overkill for mobile login use case
- **External OTP service**: Rejected - adds dependency and cost, simple OTP can be self-hosted

**Implementation Pattern**:
```java
@Service
public class OtpService {
    public String generateOtp(String mobileNumber) {
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000));
        String encryptedOtp = encryptionUtil.encrypt(otp);
        
        OtpVerification otpVerification = OtpVerification.builder()
            .mobileNumber(mobileNumber)
            .otpCode(encryptedOtp)
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .isUsed(false)
            .build();
        otpRepository.save(otpVerification);
        
        return otp; // Return plaintext for SMS sending, don't log it
    }
    
    public boolean verifyOtp(String mobileNumber, String providedOtp) {
        // Find active OTP, decrypt, constant-time compare, mark used
    }
}
```

---

### 2. JWT Refresh Token Rotation Pattern

**Decision**: Implement refresh token rotation (one-time use) with family tracking

**Rationale**:
- **Security**: Single-use refresh tokens prevent token replay attacks. If refresh token is stolen and used, legitimate user's next refresh attempt will detect anomaly.
- **Token Family**: Track token family ID to detect concurrent refresh attempts (indicates token theft). If detected, invalidate entire family.
- **Storage**: Store hashed refresh tokens in `refresh_token` table with `userId`, `tokenHash`, `expiresAt` (30 days), `isRevoked`.
- **Rotation Flow**: Client sends refresh token → verify hash → generate new access + refresh token pair → revoke old refresh token → return new pair.

**Alternatives Considered**:
- **Reusable refresh tokens**: Rejected - security risk, can't detect theft
- **No refresh tokens (short-lived access only)**: Rejected - poor UX, user has to re-authenticate frequently
- **Refresh token in cookie**: Rejected - mobile apps don't manage cookies well, explicit Authorization header better for cross-platform

**Implementation Pattern**:
```java
@Service
public class RefreshTokenService {
    public JwtAuthenticationResponse refreshAccessToken(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new InvalidTokenException());
        
        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException();
        }
        
        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateToken(storedToken.getUserId());
        String newRefreshToken = generateRefreshToken();
        
        // Revoke old, store new
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        storeRefreshToken(storedToken.getUserId(), newRefreshToken);
        
        return new JwtAuthenticationResponse(newAccessToken, newRefreshToken);
    }
}
```

---

### 3. Parent-Student Authorization Pattern

**Decision**: Service-layer authorization checks with cached linkage lookup

**Rationale**:
- **Layer**: Authorization logic in service layer (not controller) to ensure all code paths validate linkage, not just HTTP entry points.
- **Caching**: Parent-student linkages change infrequently (maybe monthly). Cache linkage set per parent with 1-hour TTL using `@Cacheable`.
- **Validation**: Before any parent accesses student data, call `parentAuthorizationService.validateAccess(parentId, studentId)`. Throws `UnauthorizedChildAccessException` if linkage missing.
- **Student auto-resolution**: For STUDENT role, auto-inject `studentId` from JWT claims, no manual parameter needed.

**Alternatives Considered**:
- **Database join on every query**: Rejected - performance overhead for read-heavy operations
- **Client-side filtering**: Rejected - insecure, never trust client
- **Spring Security custom voters**: Rejected - overkill, service-layer check simpler and testable

**Implementation Pattern**:
```java
@Service
@RequiredArgsConstructor
public class ParentAuthorizationService {
    private final ParentStudentLinkRepository linkRepository;
    
    @Cacheable(value = "parentChildLinks", key = "#parentId")
    public Set<Long> getLinkedStudentIds(Long parentId) {
        return linkRepository.findByParentId(parentId)
            .stream()
            .map(ParentStudentLink::getStudentId)
            .collect(Collectors.toSet());
    }
    
    public void validateAccess(Long parentId, Long studentId) {
        Set<Long> linkedIds = getLinkedStudentIds(parentId);
        if (!linkedIds.contains(studentId)) {
            throw new UnauthorizedChildAccessException(
                "Parent " + parentId + " is not authorized to access student " + studentId);
        }
    }
}

// Usage in service
@Service
public class AttendanceService {
    public AttendanceSummary getAttendanceSummary(Long requestingUserId, String role, Long studentId) {
        if ("PARENT".equals(role)) {
            Long parentId = parentRepository.findByUserId(requestingUserId).getId();
            parentAuthorizationService.validateAccess(parentId, studentId);
        } else if ("STUDENT".equals(role)) {
            // Auto-resolve: studentId should match requesting user's student record
            if (!studentId.equals(getStudentIdFromUserId(requestingUserId))) {
                throw new UnauthorizedAccessException();
            }
        }
        // Proceed with data fetch
    }
}
```

---

### 4. GPS Location Tracking and ETA Calculation

**Decision**: Store raw GPS pings in time-series optimized table, calculate ETA using haversine distance + historical speed

**Rationale**:
- **Storage**: `location_ping` table with (vehicleId, tripId, lat, lng, speed, heading, capturedAt, receivedAt). Index on (vehicleId, capturedAt DESC) for latest ping queries.
- **Haversine Distance**: Calculate great-circle distance between current bus location and stop coordinates. Formula: `distance = 2 * R * asin(sqrt(sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)))` where R = 6371 km.
- **ETA Calculation**: If bus moving >5 km/h, use current speed: `ETA = distance / current_speed`. If stationary or slow, use route's historical average speed for that segment.
- **Staleness Detection**: If no ping in last 2 minutes, mark location as stale and show "Last updated X minutes ago" instead of calculated ETA.
- **Batch Inserts**: If receiving pings every 10-30 seconds, consider batch insert strategy to reduce DB overhead (collect pings for 1 minute, bulk insert).

**Alternatives Considered**:
- **Google Maps Distance Matrix API**: Rejected - costly for high-frequency queries, introduces external dependency
- **Route-based ETA only**: Rejected - doesn't account for traffic, delays; live GPS essential for accuracy
- **Complex ML-based prediction**: Rejected - overkill for MVP, simple speed-based calculation sufficient

**Implementation Pattern**:
```java
@Service
public class TransportEtaService {
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return EARTH_RADIUS_KM * c;
    }
    
    public Integer calculateEtaMinutes(Long routeId, Long stopId) {
        LocationPing latestPing = locationPingRepository
            .findLatestByRouteVehicle(routeId)
            .orElse(null);
        
        if (latestPing == null || latestPing.getReceivedAt().isBefore(LocalDateTime.now().minusMinutes(2))) {
            return null; // Stale data
        }
        
        Stop stop = stopRepository.findById(stopId).orElseThrow();
        double distanceKm = calculateDistance(
            latestPing.getLatitude(), latestPing.getLongitude(),
            stop.getLatitude(), stop.getLongitude()
        );
        
        double speedKmh = latestPing.getSpeed() > 5 ? latestPing.getSpeed() : 
                          getHistoricalAverageSpeed(routeId);
        
        return (int) Math.ceil((distanceKm / speedKmh) * 60); // Convert hours to minutes
    }
}
```

---

### 5. FCM/APNs Push Notification Integration

**Decision**: Use Firebase Cloud Messaging SDK for unified push (handles both Android and iOS)

**Rationale**:
- **FCM Advantages**: Single SDK supports both Android (native) and iOS (via APNs relay). Handles token management, retry logic, and delivery confirmation.
- **Dependency**: Add `com.google.firebase:firebase-admin` to build.gradle. Initialize with service account JSON.
- **Token Storage**: Store FCM tokens in `notification_token` table per user (one user can have multiple devices). Update `lastUsedAt` on each successful send.
- **Async Sending**: Push notification sending must be `@Async` to avoid blocking business logic. Use ThreadPoolTaskExecutor with 10-20 threads.
- **Notification Types**: Absence alert, fee reminder (scheduled job), new notice (triggered on create), exam published, bus approaching stop (distance-based trigger).

**Alternatives Considered**:
- **Direct APNs for iOS**: Rejected - requires separate implementation, FCM simplifies to single integration
- **Twilio Notify service**: Rejected - adds cost, FCM is free tier available
- **WebSocket for real-time**: Rejected - push notifications more reliable for mobile apps (work when app backgrounded)

**Implementation Pattern**:
```java
@Configuration
public class FcmConfig {
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
        return FirebaseApp.initializeApp(options);
    }
}

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationTokenRepository tokenRepository;
    private final NotificationLogRepository logRepository;
    
    @Async
    public void sendPushNotification(Long userId, String title, String body, String type) {
        List<NotificationToken> tokens = tokenRepository.findByUserIdAndIsActiveTrue(userId);
        
        for (NotificationToken token : tokens) {
            try {
                Message message = Message.builder()
                    .setToken(token.getToken())
                    .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                    .putData("type", type)
                    .build();
                
                String response = FirebaseMessaging.getInstance().send(message);
                logDelivery(userId, type, title, body, "sent");
                
                token.setLastUsedAt(LocalDateTime.now());
                tokenRepository.save(token);
            } catch (FirebaseMessagingException e) {
                if (e.getErrorCode().equals("NOT_FOUND") || e.getErrorCode().equals("INVALID_ARGUMENT")) {
                    // Token invalid, mark inactive
                    token.setActive(false);
                    tokenRepository.save(token);
                }
                logDelivery(userId, type, title, body, "failed");
            }
        }
    }
}
```

---

### 6. Homework File Upload and Cloud Storage

**Decision**: Use AWS S3 (or compatible service) with presigned URL pattern for direct upload from mobile

**Rationale**:
- **Direct Upload**: Mobile app uploads file directly to S3 (not via backend proxy) to reduce backend bandwidth and latency.
- **Presigned URLs**: Backend generates short-lived (5 minutes) presigned S3 PUT URL. Mobile uploads file to that URL. On success, mobile sends S3 object key to backend for homework submission record.
- **Storage**: Store S3 object keys/URLs in `homework_submission.submission_file_urls` JSON array field.
- **File Validation**: Enforce file type (image/*, application/pdf) and size (max 10MB) in presigned URL policy. Backend validates file existence in S3 before creating submission record.
- **Security**: S3 bucket private with no public access. All file retrieval via presigned GET URLs (also short-lived).

**Alternatives Considered**:
- **Upload via backend multipart**: Rejected - wastes backend resources, limits concurrent uploads
- **Store files in MySQL BLOB**: Rejected - bloats database, poor performance for large files
- **Local file system storage**: Rejected - not scalable, complicates horizontal scaling

**Implementation Pattern**:
```java
@Service
@RequiredArgsConstructor
public class HomeworkSubmissionService {
    private final AmazonS3 s3Client;
    private final String bucketName = "sanchalan-homework-submissions";
    
    public PresignedUrlResponse generateUploadUrl(Long homeworkId, String fileName) {
        String objectKey = String.format("homework/%d/%s/%s", 
            homeworkId, UUID.randomUUID(), fileName);
        
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 5); // 5 minutes
        
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey)
            .withMethod(HttpMethod.PUT)
            .withExpiration(expiration);
        
        URL presignedUrl = s3Client.generatePresignedUrl(request);
        
        return PresignedUrlResponse.builder()
            .uploadUrl(presignedUrl.toString())
            .objectKey(objectKey)
            .expiresAt(expiration.toInstant())
            .build();
    }
    
    public HomeworkSubmission submitHomework(Long homeworkId, Long studentId, List<String> fileKeys) {
        // Validate files exist in S3
        for (String key : fileKeys) {
            if (!s3Client.doesObjectExist(bucketName, key)) {
                throw new FileNotFoundException("File not found: " + key);
            }
        }
        
        // Create submission record
        HomeworkSubmission submission = HomeworkSubmission.builder()
            .homeworkId(homeworkId)
            .studentId(studentId)
            .submittedAt(LocalDateTime.now())
            .submissionFileUrls(fileKeys)
            .status("submitted")
            .build();
        
        return homeworkSubmissionRepository.save(submission);
    }
    
    public String generateDownloadUrl(String objectKey) {
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 15); // 15 minutes
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey)
            .withMethod(HttpMethod.GET)
            .withExpiration(expiration);
        return s3Client.generatePresignedUrl(request).toString();
    }
}
```

---

### 7. High-Frequency Time-Series Data for GPS Pings

**Decision**: Optimize `location_ping` table with partitioning and retention policy

**Rationale**:
- **Write Volume**: 50-100 buses × 1 ping every 30 seconds = ~100-200 inserts/minute during peak hours. Over 30 days = ~8-10 million rows.
- **Table Partitioning**: Partition `location_ping` table by `capturedAt` date (daily or weekly partitions). Simplifies data purging - drop old partitions instead of DELETE queries.
- **Indexes**: Composite index on (vehicleId, capturedAt DESC) for "latest ping" queries. Avoid full table scans.
- **Retention**: Keep only last 30 days of GPS data (privacy + storage). Automated cleanup job drops partitions older than 30 days.
- **Archival**: If historical analysis needed, archive old partitions to S3 Parquet format before dropping.

**Alternatives Considered**:
- **Separate time-series database (InfluxDB, TimescaleDB)**: Rejected - adds operational complexity, Postgres partitioning sufficient for this scale
- **MongoDB for flexible schema**: Rejected - GPS data is highly structured, relational model appropriate
- **No partitioning**: Rejected - table will grow unbounded, query performance degrades over time

**Implementation Pattern**:
```sql
-- V13__create_transport_tables.sql
CREATE TABLE location_ping (
    id BIGSERIAL NOT NULL,
    vehicle_id BIGINT NOT NULL,
    trip_id BIGINT,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    speed DECIMAL(5,2),
    heading SMALLINT,
    accuracy DECIMAL(6,2),
    captured_at TIMESTAMP NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, captured_at)  -- Include partitioning column in PK
) PARTITION BY RANGE (captured_at);

-- Create initial partitions (automate via scheduled job)
CREATE TABLE location_ping_2026_02 PARTITION OF location_ping
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- Index for latest ping queries
CREATE INDEX idx_location_ping_vehicle_time ON location_ping (vehicle_id, captured_at DESC);
```

```java
@Service
public class LocationPingCleanupJob {
    @Scheduled(cron = "0 0 3 * * *") // Run daily at 3 AM
    public void dropOldPartitions() {
        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        String partitionName = "location_ping_" + cutoffDate.format(DateTimeFormatter.ofPattern("yyyy_MM"));
        
        // Archive to S3 if needed, then drop
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + partitionName);
    }
}
```

---

### 8. Caching Strategy for Authorization Lookups

**Decision**: Use Spring Cache with Caffeine for in-memory caching of parent-student linkages and route assignments

**Rationale**:
- **Linkage Cache**: Parent-student linkages change infrequently (monthly at most). Cache linkage set per parent for 1 hour. Evict on linkage modification via `@CacheEvict`.
- **Route Assignment Cache**: Student transport assignments also stable. Cache per student for 6 hours. Evict on assignment change.
- **Cache Provider**: Caffeine (local in-memory) sufficient for single instance. If multi-instance, consider Redis for shared cache (future optimization).
- **Cache Warming**: Optional - preload active parent linkages into cache at startup. Low priority, lazy loading acceptable.
- **Monitoring**: Add cache hit/miss metrics via Micrometer for observability.

**Alternatives Considered**:
- **No caching**: Rejected - every attendance/homework query would hit linkage table, unnecessary DB load
- **Application-level HashMap cache**: Rejected - Spring Cache provides standardized annotations and eviction policies
- **Redis for distributed cache**: Deferred to Phase 2 - single instance deployment sufficient initially

**Implementation Pattern**:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "parentChildLinks", "studentTransportAssignments"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats());
        return cacheManager;
    }
}

@Service
public class ParentAuthorizationService {
    @Cacheable(value = "parentChildLinks", key = "#parentId")
    public Set<Long> getLinkedStudentIds(Long parentId) {
        return linkRepository.findByParentId(parentId)
            .stream()
            .map(ParentStudentLink::getStudentId)
            .collect(Collectors.toSet());
    }
    
    @CacheEvict(value = "parentChildLinks", key = "#parentId")
    public void evictLinkageCache(Long parentId) {
        // Called when admin modifies parent-student linkages
    }
}
```

---

## Summary of Technical Decisions

| Area | Decision | Libraries/Tools |
|------|----------|----------------|
| OTP Generation | SecureRandom 6-digit, AES-256 encryption, rate limiting | Java SecureRandom, Spring Crypto |
| Refresh Tokens | One-time use rotation with family tracking | JWT, BCrypt hashing |
| Authorization | Service-layer validation with cached linkage lookup | Spring Cache, Caffeine |
| GPS Tracking | Store raw pings, haversine distance, speed-based ETA | Custom haversine formula |
| Push Notifications | Firebase Cloud Messaging (FCM) for Android + iOS | firebase-admin SDK, @Async |
| File Upload | Presigned S3 URLs for direct upload from mobile | AWS SDK for Java, S3 |
| Time-Series Data | Postgres table partitioning by date, 30-day retention | Flyway migrations, @Scheduled |
| Caching | Caffeine in-memory cache for linkages and assignments | Spring Cache, Caffeine |

All decisions prioritize security, performance, and maintainability while leveraging existing Spring Boot ecosystem tools. No external service dependencies beyond cloud storage (S3) and push notifications (FCM), both of which are industry-standard choices.

---

**Phase 0 Complete** - All technical unknowns resolved. Ready for Phase 1 (data model and contracts).
