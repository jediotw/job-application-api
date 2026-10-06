package com.example.jobapplicationapi.user.service;

import com.example.jobapplicationapi.config.JwtService;
import com.example.jobapplicationapi.exception.AuthenticationException;
import com.example.jobapplicationapi.exception.DataConflictException;
import com.example.jobapplicationapi.user.dto.LoginRequest;
import com.example.jobapplicationapi.user.dto.LoginResponse;
import com.example.jobapplicationapi.user.dto.RegisterRequest;
import com.example.jobapplicationapi.user.dto.UserResponse;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public UserService(
      UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {

    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public UserResponse register(RegisterRequest request) {

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new DataConflictException("Email already exists");
    }

    String passwordHash = passwordEncoder.encode(request.getPassword());

    User user = new User();

    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordHash);
    user.setRole("CANDIDATE");

    User savedUser = userRepository.save(user);

    return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
  }

  public LoginResponse login(LoginRequest request) {

    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

    boolean passwordMatches =
        passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

    if (!passwordMatches) {
      throw new AuthenticationException("Invalid email or password");
    }

    String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

    return new LoginResponse(token, user.getId(), user.getEmail(), user.getRole());
  }
}
