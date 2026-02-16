package com.cm.sanchalak.config;

import com.cm.sanchalak.security.CustomUserDetailsService;
import com.cm.sanchalak.platform.auth.PlatformUserDetailsService;
import com.cm.sanchalak.security.JwtAuthenticationEntryPoint;
import com.cm.sanchalak.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.core.annotation.Order;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService;
        private final JwtAuthenticationEntryPoint unauthorizedHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final PlatformUserDetailsService platformUserDetailsService;

        @Bean
        public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider platformProvider = new DaoAuthenticationProvider(platformUserDetailsService);
                platformProvider.setPasswordEncoder(passwordEncoder);

                DaoAuthenticationProvider customProvider = new DaoAuthenticationProvider(customUserDetailsService);
                customProvider.setPasswordEncoder(passwordEncoder);

                return new ProviderManager(platformProvider, customProvider);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
                FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
                registration.setEnabled(false);
                return registration;
        }

        @Bean
        @Order(1)
        public SecurityFilterChain platformFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/api/platform/**")
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(unauthorizedHandler))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/api/platform/v1/auth/**", "/error").permitAll()
                                                .anyRequest().authenticated())
                                .userDetailsService(platformUserDetailsService);

                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(unauthorizedHandler))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/",
                                                                "/favicon.ico",
                                                                "/*.png",
                                                                "/*.gif",
                                                                "/*.svg",
                                                                "/*.jpg",
                                                                "/*.html",
                                                                "/*.css",
                                                                "/*.js",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/api/auth/**",
                                                                "/api/user/checkUsernameAvailability",
                                                                "/api/user/checkEmailAvailability",
                                                                "/ping",
                                                                "/error")
                                                .permitAll()
                                                .anyRequest()
                                                .authenticated())
                                .userDetailsService(customUserDetailsService);

                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("*"));
                configuration.setAllowedMethods(List.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
