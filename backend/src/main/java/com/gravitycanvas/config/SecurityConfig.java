package com.gravitycanvas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration class.
 * Defines Role-Based Access Control (RBAC) rules for Project Managers, Team Leads, Developers, and Testers.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                
                .requestMatchers("/api/auth/me").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD", "TEAM_MEMBER", "TESTER")
                
                // Project Manager User Creation & User Listing
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("PROJECT_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/users/change-password").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD", "TEAM_MEMBER", "TESTER")
                .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD", "TEAM_MEMBER", "TESTER")

                // Settings Endpoints
                .requestMatchers(HttpMethod.GET, "/api/settings").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD", "TEAM_MEMBER", "TESTER")
                .requestMatchers(HttpMethod.PUT, "/api/settings").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD")

                // Task Creation: Restricted to PROJECT_MANAGER and TEAM_LEAD
                .requestMatchers(HttpMethod.POST, "/api/tasks").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD")

                // Approval Queue Endpoints
                .requestMatchers(HttpMethod.GET, "/api/tasks/pending").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD")
                .requestMatchers(HttpMethod.PUT, "/api/tasks/*/approve").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD")
                .requestMatchers(HttpMethod.PUT, "/api/tasks/*/reject").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD")

                // Restricted Task Operations (Project Manager & Team Lead Only)
                .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD")
                .requestMatchers(HttpMethod.PUT, "/api/tasks/*/deadline").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD")
                .requestMatchers(HttpMethod.PUT, "/api/tasks/*/reassign").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD")
                .requestMatchers(HttpMethod.POST, "/api/tasks/*/ping").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD")

                // Shared Task Operations (All Roles: Project Manager, Team Lead, Developer, Tester)
                .requestMatchers(HttpMethod.GET, "/api/tasks/**").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD", "TEAM_MEMBER", "TESTER")
                .requestMatchers(HttpMethod.PUT, "/api/tasks/*/status").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD", "TEAM_MEMBER", "TESTER")
                .requestMatchers(HttpMethod.POST, "/api/tasks/*/defibrillate").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD", "TEAM_MEMBER", "TESTER")
                .requestMatchers(HttpMethod.POST, "/api/tasks/simulate/**").hasAnyRole("PROJECT_MANAGER", "TEAM_LEAD", "TEAM_MEMBER", "TESTER")

                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:4200", "http://localhost:*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
