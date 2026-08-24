package com.mdsaifullah.smartinterview.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Password Encryption
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // =========================
                // Public Pages
                // =========================
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/register.html",
                    "/css/**",
                    "/js/**"
                ).permitAll()

                // =========================
                // Login & Register
                // =========================
                .requestMatchers(
                    "/api/users/login",
                    "/api/users/register"
                ).permitAll()

                // Only ADMIN can view the list of all users
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/users"
                ).hasRole("ADMIN")

                // =========================
                // Question APIs
                // =========================

                // Anyone logged in can view questions
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/questions",
                    "/api/questions/**"
                ).authenticated()

                // Only ADMIN can create/update/delete questions
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/questions"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/questions/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/questions/**"
                ).hasRole("ADMIN")

                // =========================
                // Quiz APIs
                // =========================

                // Logged-in USER/ADMIN can view quizzes
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/quizzes",
                    "/api/quizzes/**"
                ).authenticated()

                // Only ADMIN can create quiz
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/quizzes"
                ).hasRole("ADMIN")

                // Only ADMIN can update quiz
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/quizzes/**"
                ).hasRole("ADMIN")

                // Only ADMIN can delete quiz
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/quizzes/**"
                ).hasRole("ADMIN")

                // =========================
                // Result APIs
                // =========================

                // Logged-in users can access results and interviews
                .requestMatchers(
                    "/api/results/**",
                    "/api/interviews/**"
                ).authenticated()

                // =========================
                // Other APIs
                // =========================
                .requestMatchers(
                    "/api/**"
                ).authenticated()

                .anyRequest().permitAll()
            )

            // JWT Filter
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}