package com.cm.sanchalak.platform.auth;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformUserDetailsService implements UserDetailsService {

    private final PlatformUserRepository platformUserRepository;

    public PlatformUserDetailsService(PlatformUserRepository platformUserRepository) {
        this.platformUserRepository = platformUserRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        PlatformUser user = platformUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + email));

        return PlatformUserDetails.create(user);
    }

    @Transactional
    public UserDetails loadUserById(UUID id) {
        PlatformUser user = platformUserRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));

        return PlatformUserDetails.create(user);
    }
}
