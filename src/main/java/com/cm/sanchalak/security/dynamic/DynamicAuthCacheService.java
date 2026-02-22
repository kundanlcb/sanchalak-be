package com.cm.sanchalak.security.dynamic;

import com.cm.sanchalak.entity.ApiEndpoint;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.repository.ApiEndpointRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicAuthCacheService {

    private final ApiEndpointRepository apiEndpointRepository;

    // In-memory cache map: Structure is Map<Method, Map<UrlPattern, Set<RoleName>>>
    // Example: Map<"GET", Map<"/api/academic/**", Set<"ROLE_ADMIN",
    // "ROLE_TEACHER">>>
    private final Map<String, Map<String, Set<String>>> authCache = new ConcurrentHashMap<>();

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Loads all endpoints and their mapped roles from the database into the
     * ConcurrentHashMap cache.
     * This method is called at startup and should be called whenever the role
     * mappings are updated via the UI.
     */
    @PostConstruct
    @Transactional(readOnly = true)
    public void refreshCache() {
        log.info("Refreshing Dynamic Auth Cache from Database...");
        authCache.clear();

        List<ApiEndpoint> allEndpoints = apiEndpointRepository.findAll();

        for (ApiEndpoint endpoint : allEndpoints) {
            String method = endpoint.getMethod();
            String urlPattern = endpoint.getUrlPattern();
            Set<String> roleNames = endpoint.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toSet());

            authCache.computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                    .put(urlPattern, roleNames);

            if (roleNames.isEmpty()) {
                log.debug("Endpoint {} {} has no assigned roles. It will be inaccessible.", method, urlPattern);
            }
        }

        log.info("Dynamic Auth Cache loaded with {} endpoints.", allEndpoints.size());
    }

    /**
     * Looks up the required roles for a specific HTTP method and URL.
     * Because URLs might contain path variables (e.g., /api/terms/1), we use
     * AntPathMatcher
     * against all cached patterns (e.g., /api/terms/{id}) for the given method.
     * 
     * @param method     The HTTP method (GET, POST, etc.)
     * @param requestUri The actual request URI.
     * @return A Set of role names allowed to access this URI.
     */
    public Set<String> getRequiredRoles(String method, String requestUri) {
        Map<String, Set<String>> methodPatterns = authCache.get(method);

        if (methodPatterns == null || methodPatterns.isEmpty()) {
            return Set.of(); // Empty set implies Deny Default
        }

        // Iterate through known patterns for this method and find a match
        for (Map.Entry<String, Set<String>> entry : methodPatterns.entrySet()) {
            String pattern = entry.getKey();
            if (pathMatcher.match(pattern, requestUri)) {
                return entry.getValue(); // Return the roles for the matched pattern
            }
        }

        return Set.of(); // No matching pattern found
    }
}
