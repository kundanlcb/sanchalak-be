package com.cm.sanchalak.security.dynamic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicEndpointAuthorizationManager implements AuthorizationManager<Object> {

    private final DynamicAuthCacheService authCacheService;

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authenticationSupplier, Object object) {
        RequestAuthorizationContext context = (RequestAuthorizationContext) object;
        HttpServletRequest request = context.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 1. Ask Cache: "What roles are allowed to access METHOD + URL?"
        Set<String> requiredRoles = authCacheService.getRequiredRoles(method, uri);

        // 2. Secure by Default: If endpoint isn't mapped to ANY role, deny access.
        if (requiredRoles.isEmpty()) {
            log.warn("Access Denied: Endpoint {} {} is not mapped to any roles.", method, uri);
            return new AuthorizationDecision(false);
        }

        Authentication authentication = authenticationSupplier.get();

        // 3. Ensure the user is authenticated at all
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Access Denied: Unauthenticated user attempted to access {} {}", method, uri);
            return new AuthorizationDecision(false);
        }

        // 4. Extract user roles from their Authentication Token
        List<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 5. Check if the user has any of the required roles
        boolean hasAccess = requiredRoles.stream().anyMatch(userRoles::contains);

        // Platform Admin Bypass: Allow 'OWNER' and 'ADMIN' full access
        if (userRoles.contains("OWNER") || userRoles.contains("ROLE_OWNER") || userRoles.contains("ROLE_ADMIN")) {
            hasAccess = true;
        }

        if (!hasAccess) {
            log.warn("Access Denied: User with roles {} attempted to access {} {}. Required roles: {}",
                    userRoles, method, uri, requiredRoles);
        }

        return new AuthorizationDecision(hasAccess);
    }
}
