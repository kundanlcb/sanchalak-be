package com.cm.sanchalak.interceptor;

import com.cm.sanchalak.dto.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, RateLimitInfo> otpRequestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 3;
    private static final long TIME_WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(15);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // Apply rate limiting only to OTP endpoints
        if (uri.contains("/api/auth/otp/request") || uri.contains("/api/auth/otp/verify")) {
            String clientIp = getClientIp(request);
            String key = clientIp + ":" + uri; // Key by IP and endpoint

            long currentTime = System.currentTimeMillis();
            otpRequestCounts.compute(key, (k, v) -> {
                if (v == null || (currentTime - v.startTime > TIME_WINDOW_MILLIS)) {
                    // Reset window
                    return new RateLimitInfo(1, currentTime);
                } else {
                    // Increment count
                    v.count++;
                    return v;
                }
            });

            RateLimitInfo info = otpRequestCounts.get(key);
            if (info.count > MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResult.error("RATE_LIMIT_EXCEEDED", "Too many attempts. Please try again after 15 minutes.")
                ));
                return false;
            }
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitInfo {
        int count;
        long startTime;

        RateLimitInfo(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }
    }
}
