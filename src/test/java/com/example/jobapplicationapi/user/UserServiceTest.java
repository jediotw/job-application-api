package com.example.jobapplicationapi.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.jobapplicationapi.config.JwtService;
import com.example.jobapplicationapi.exception.AuthenticationException;
import com.example.jobapplicationapi.exception.DataConflictException;
import com.example.jobapplicationapi.user.dto.LoginRequest;
import com.example.jobapplicationapi.user.dto.LoginResponse;
import com.example.jobapplicationapi.user.dto.RegisterRequest;
import com.example.jobapplicationapi.user.dto.UserResponse;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtService jwtService;

  @InjectMocks private UserService userService;

  @Test
  void shouldRegisterUser() {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("saurabh@example.com");
    request.setPassword("mysecretpassword");

    User user = new User();
    user.setId(1L);
    user.setEmail("saurabh@example.com");
    user.setPasswordHash("hashed-password");
    user.setRole("CANDIDATE");

    when(userRepository.findByEmail("saurabh@example.com")).thenReturn(Optional.empty());

    when(passwordEncoder.encode("mysecretpassword")).thenReturn("hashed-password");

    when(userRepository.save(any(User.class))).thenReturn(user);

    UserResponse response = userService.register(request);

    assertEquals(1L, response.getId());
    assertEquals("saurabh@example.com", response.getEmail());
    assertEquals("CANDIDATE", response.getRole());
  }

  @Test
  void shouldRejectDuplicateEmail() {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("saurabh@example.com");
    request.setPassword("mysecretpassword");

    User existingUser = new User();
    existingUser.setId(1L);
    existingUser.setEmail("saurabh@example.com");

    when(userRepository.findByEmail("saurabh@example.com")).thenReturn(Optional.of(existingUser));

    DataConflictException exception =
        assertThrows(DataConflictException.class, () -> userService.register(request));

    assertEquals("Email already exists", exception.getMessage());
  }

  @Test
  void shouldLoginUser() {
    LoginRequest request = new LoginRequest();
    request.setEmail("saurabh@example.com");
    request.setPassword("mysecretpassword");

    User user = new User();
    user.setId(1L);
    user.setEmail("saurabh@example.com");
    user.setPasswordHash("hashed-password");
    user.setRole("CANDIDATE");

    when(userRepository.findByEmail("saurabh@example.com")).thenReturn(Optional.of(user));

    when(passwordEncoder.matches("mysecretpassword", "hashed-password")).thenReturn(true);

    when(jwtService.generateToken(1L, "saurabh@example.com", "CANDIDATE"))
        .thenReturn("test.jwt.token");

    LoginResponse response = userService.login(request);

    assertEquals("test.jwt.token", response.getToken());
    assertEquals(1L, response.getId());
    assertEquals("saurabh@example.com", response.getEmail());
    assertEquals("CANDIDATE", response.getRole());
  }

  @Test
  void shouldRejectWrongPassword() {
    LoginRequest request = new LoginRequest();
    request.setEmail("saurabh@example.com");
    request.setPassword("wrongpassword");

    User user = new User();
    user.setId(1L);
    user.setEmail("saurabh@example.com");
    user.setPasswordHash("hashed-password");
    user.setRole("CANDIDATE");

    when(userRepository.findByEmail("saurabh@example.com")).thenReturn(Optional.of(user));

    when(passwordEncoder.matches("wrongpassword", "hashed-password")).thenReturn(false);

    AuthenticationException exception =
        assertThrows(AuthenticationException.class, () -> userService.login(request));

    assertEquals("Invalid email or password", exception.getMessage());
  }

  @Test
  void shouldRejectUnknownEmail() {
    LoginRequest request = new LoginRequest();
    request.setEmail("unknown@example.com");
    request.setPassword("mysecretpassword");

    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    AuthenticationException exception =
        assertThrows(AuthenticationException.class, () -> userService.login(request));

    assertEquals("Invalid email or password", exception.getMessage());
  }
}
