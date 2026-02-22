Technical Specification: Dynamic Endpoint Authorization System
1. Overview
The current authorization model in Sanchalak relies heavily on hardcoded @PreAuthorize annotations at the controller level (e.g., @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL_ADMIN')")).

This specification proposes replacing the static, annotation-based Role-Based Access Control (RBAC) with a completely dynamic, database-driven API authorization system. All @PreAuthorize annotations will be removed from controllers. Authorization rules will be evaluated dynamically at runtime using a custom Spring Security 6 AuthorizationManager, allowing administrators to manage API access via a UI without requiring application restarts or code changes.

2. Goals
Zero Annotations: Remove all @PreAuthorize tags from @RestController classes to separate business logic from security rules.
Dynamic Configuration: Enable runtime assignment of endpoints to specific roles via the database (and eventually a frontend UI).
Auto-Discovery: Automatically scan and register new API endpoints into the database on application startup.
High Performance: Ensure authorization checks do not degrade API response times by employing heavy in-memory caching (e.g., Redis or Caffeine).
Secure by Default: Deny access to any registered endpoint that does not have explicit role mappings unless explicitly whitelisted in the security configuration.
3. Database Architecture
We will introduce three new entities to manage the dynamic mappings.

3.1 ApiEndpoint Entity
Stores information about every available API route in the application.

id
 (Long, PK)
method (String) - Ex: GET, POST, DELETE
urlPattern (String) - Ex: /api/academic/terms/**
moduleName (String) - Used for grouping in the UI (e.g., Academics, 
Attendance
)
description (String) - Human-readable name (e.g., "Delete Exam Term")
3.2 
Role
 Entity (Existing)
Updates to ensure it can map to endpoints.

id
 (Long, PK)
name
 (String) - Ex: ROLE_ADMIN, ROLE_TEACHER
3.3 EndpointRoleMapping Entity (Join Table)
Links an ApiEndpoint to the 
Role
s allowed to access it.

endpoint_id (FK to ApiEndpoint)
role_id (FK to 
Role
)
4. Implementation Details
Step 1: Schema Updates
Create Flyway/Liquibase migration scripts (or rely on Hibernate ddl-auto=update for development) to generate the api_endpoint and endpoint_role_mapping tables.

Step 2: Auto-Discovery Scanner (EndpointScannerListener)
Implement an ApplicationListener<ContextRefreshedEvent> that executes on startup:

Inject RequestMappingHandlerMapping.
Iterate over all registered HandlerMethods.
Extract the HTTP Method and URL pattern.
If the endpoint does not exist in the ApiEndpoint table, insert it.
Optional Enhancement: Read custom annotations (e.g., @ApiDescription("Delete Exam Term")) from the controller methods to populate the description and moduleName fields automatically.
Step 3: Global Caching Layer
Because authorization happens on every request, querying the database is not viable.

Implement a DynamicAuthCacheService.
On application startup (or cache refresh event), load SELECT urlPattern, method, requiredRoles FROM EndpointRoleMapping into a fast, thread-safe memory structure (e.g., a ConcurrentHashMap or a Caffeine Cache).
Structure the cache map for fast 
O(1)
 or 
O(N)
 matching based on AntPathMatchers.
Step 4: Custom AuthorizationManager
Implement the core logic that replaces @PreAuthorize:

java
@Component
public class DynamicEndpointAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    
    @Autowired
    private DynamicAuthCacheService authCacheService;
    
    @Autowired
    private AntPathMatcher pathMatcher;
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        // 1. Get required roles for this URI from Cache
        List<String> requiredRoles = authCacheService.getRequiredRoles(method, uri);
        // 2. If endpoint isn't mapped, deny by default (Secure by default approach)
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return new AuthorizationDecision(false); 
        }
        // 3. Extract user roles from JWT/Authentication object
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated()) {
             return new AuthorizationDecision(false);
        }
        
        List<String> userRoles = auth.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .toList();
        // 4. Check intersection
        boolean hasAccess = requiredRoles.stream().anyMatch(userRoles::contains);
        
        return new AuthorizationDecision(hasAccess);
    }
}
Step 5: 
SecurityConfig
 Update
Wire the manager into the Spring Security filter chain:

java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // 1. Explicit UI/Public Whitelists
            .requestMatchers("/api/auth/**", "/swagger-ui/**").permitAll()
            
            // 2. All other requests routed to Dynamic Manager
            .anyRequest().access(dynamicEndpointAuthorizationManager)
        );
        // ... JWT Filter setup ...
        
    return http.build();
}
Step 6: Controller Cleaning (The Refactor)
Perform a global find-and-replace to strip @PreAuthorize from all controller methods.
Remove any imports for org.springframework.security.access.prepost.PreAuthorize.
Step 7: UI Integration (Permission Management APIs)
To allow the frontend to manage these permissions, we need a dedicated controller (e.g., PermissionManagementController).

Required Endpoints:

GET /api/system/endpoints: Returns a list of all discovered and registered ApiEndpoint entities, grouped by moduleName.
GET /api/system/roles/{roleId}/endpoints: Returns a list of endpoint IDs that are currently mapped to the given role.
POST /api/system/roles/{roleId}/endpoints: Accepts a list of endpoint IDs to completely replace the role's current mappings.
Crucial Cache Invalidation: When the POST mapping is updated, the backend must explicitly invalidate or rebuild the DynamicAuthCacheService. This ensures the changes take effect immediately without needing to restart the backend server.

5. Migration Strategy & Risks
Risks
Path Matching Performance: AntPathMatcher (used to match /api/users/123 against /api/users/{id}) can become a bottleneck if the cache contains thousands of routes.
Mitigation: Use Spring's newer PathPatternParser which is significantly faster than AntPathMatcher.
Accidental Exposure: If the 
SecurityConfig
 permits all, and the Dynamic Manager fails to correctly identify an unmapped route, a sensitive API could become public.
Mitigation: Enforce "Secure by Default". If a route isn't explicitly mapped to a role in the cache, the manager must return false (Deny).
Rollout Plan
Implement internal Entities, Caching, and Auto-Discovery.
Run the application locally to let the auto-discovery seed the database.
Write SQL scripts to manually insert the baseline role mappings (matching the current @PreAuthorize rules) into the endpoint_role_mapping table.
Implement the AuthorizationManager and switch 
SecurityConfig
 over.
Remove a few @PreAuthorize tags to test.
Strip all @PreAuthorize tags globally.
Run end-to-end integration tests to verify no unauthorized access is permitted.

Comment
⌥⌘M
