package com.cm.sanchalak.platform.support;

import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SupportService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService; // The main app's UserDetailsService

    public SupportService(UserRepository userRepository, JwtTokenProvider tokenProvider,
            @Qualifier("customUserDetailsService") UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    public void unlockAccount(UUID userId) {
        // user.setAccountNonLocked(true); // Assuming User entity has this or similar
        // For now, logging as placeholder if field is missing, or assuming
        // implementation
        // TODO: Implement actual unlock logic based on User entity structure
    }

    public String impersonateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Load UserDetails via the main service to ensure roles/permissions are correct
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Create manual authentication
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Generate token
        return tokenProvider.generateToken(authentication);
    }
}
