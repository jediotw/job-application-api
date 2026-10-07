package com.example.jobapplicationapi.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

    http.csrf(csrf -> csrf.disable())
        .cors(cors -> {})
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/register", "/auth/login")
                    .permitAll()

                    // Candidate
                    .requestMatchers("/candidates/**")
                    .hasRole("CANDIDATE")

                    // Company
                    .requestMatchers(HttpMethod.GET, "/companies/**")
                    .hasAnyRole("CANDIDATE", "RECRUITER")
                    .requestMatchers("/companies/**")
                    .hasRole("RECRUITER")

                    // Job
                    .requestMatchers(HttpMethod.GET, "/jobs/**")
                    .hasAnyRole("CANDIDATE", "RECRUITER")
                    .requestMatchers("/jobs/**")
                    .hasRole("RECRUITER")

                    // Application
                    .requestMatchers(HttpMethod.GET, "/applications/**")
                    .hasAnyRole("CANDIDATE", "RECRUITER")
                    .requestMatchers(HttpMethod.POST, "/applications/**")
                    .hasRole("CANDIDATE")
                    .requestMatchers("/applications/**")
                    .hasAnyRole("CANDIDATE", "RECRUITER")
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptions ->
                exceptions.authenticationEntryPoint(
                    (request, response, exception) -> {
                      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                      response
                          .getWriter()
                          .write("{\"status\":401,\"message\":\"Unauthenticated\"}");
                    }))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
