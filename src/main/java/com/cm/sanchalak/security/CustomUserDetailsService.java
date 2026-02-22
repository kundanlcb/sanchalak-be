package com.cm.sanchalak.security;

import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.platform.school.SchoolUserRepository;
import com.cm.sanchalak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;
        private final SchoolUserRepository schoolUserRepository;

        @Override
        @Transactional
        public UserDetails loadUserByUsername(String email)
                        throws UsernameNotFoundException {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with email : " + email));

                return createUserPrincipalWithResolvedSchoolId(user);
        }

        @Transactional
        public UserDetails loadUserById(UUID id) {
                User user = userRepository.findById(id).orElseThrow(
                                () -> new UsernameNotFoundException("User not found with id : " + id));

                return createUserPrincipalWithResolvedSchoolId(user);
        }

        private UserPrincipal createUserPrincipalWithResolvedSchoolId(User user) {
                UserPrincipal principal = UserPrincipal.create(user);
                if (principal.getSchoolId() == null) {
                        schoolUserRepository.findByUserId(user.getId())
                                        .ifPresent(schoolUser -> {
                                                principal.setSchoolId(schoolUser.getSchoolId());
                                        });
                }
                return principal;
        }
}
