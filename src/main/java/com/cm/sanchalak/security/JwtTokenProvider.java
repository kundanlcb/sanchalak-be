package com.cm.sanchalak.security;

import com.cm.sanchalak.platform.auth.PlatformUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String userId;
        String schoolId = null;

        if (principal instanceof UserPrincipal userPrincipal) {
            userId = userPrincipal.getId().toString();
            if (userPrincipal.getSchoolId() != null) {
                schoolId = userPrincipal.getSchoolId().toString();
            }
        } else if (principal instanceof PlatformUserDetails platformUserDetails) {
            userId = platformUserDetails.getId().toString();
        } else {
            throw new IllegalArgumentException("Unknown principal type: " + principal.getClass());
        }

        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtExpirationInMs);

        JwtBuilder builder = Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey());

        if (schoolId != null) {
            builder.claim("schoolId", schoolId);
        }

        return builder.compact();
    }

    public UUID getUserIdFromJWT(String token) {
        Claims claims = getClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public UUID getSchoolIdFromJWT(String token) {
        Claims claims = getClaims(token);
        String schoolId = claims.get("schoolId", String.class);
        return schoolId != null ? UUID.fromString(schoolId) : null;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}
