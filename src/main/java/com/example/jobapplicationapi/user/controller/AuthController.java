package com.example.jobapplicationapi.user.controller;

import com.example.jobapplicationapi.user.dto.LoginRequest;
import com.example.jobapplicationapi.user.dto.LoginResponse;
import com.example.jobapplicationapi.user.dto.RegisterRequest;
import com.example.jobapplicationapi.user.dto.UserResponse;
import com.example.jobapplicationapi.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  public UserResponse register(@Valid @RequestBody RegisterRequest request) {
    UserResponse response = userService.register(request);
    return response;
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = userService.login(request);
    return response;
  }
}
