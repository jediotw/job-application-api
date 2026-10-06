package com.example.jobapplicationapi.config;

import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component // Create and manage an instance of this class.So Spring can inject:JwtService
// jwtService,UserRepository userRepository through the constructor.
// It is a Spring-provided base class for filters that should execute once for a request.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authorizationHeader.substring(7);

    String email = jwtService.extractEmail(token);

    User user = userRepository.findByEmail(email).orElse(null);

    if (user != null) {
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              user.getEmail(), null, java.util.Collections.emptyList());

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    // I've finished processing the request. Continue to the next filter/controller.
    filterChain.doFilter(request, response);
  }
}
