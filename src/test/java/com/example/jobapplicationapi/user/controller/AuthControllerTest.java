package com.example.jobapplicationapi.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jobapplicationapi.config.JwtAuthenticationFilter;
import com.example.jobapplicationapi.exception.AuthenticationException;
import com.example.jobapplicationapi.exception.DataConflictException;
import com.example.jobapplicationapi.user.dto.LoginResponse;
import com.example.jobapplicationapi.user.dto.UserResponse;
import com.example.jobapplicationapi.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void shouldRegisterUser() throws Exception {

    UserResponse response = new UserResponse(1L, "saurabh@example.com", "CANDIDATE");

    when(userService.register(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "email": "saurabh@example.com",
                                        "password": "mysecretpassword"
                                    }
                                    """))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    """
                                    {
                                        "id": 1,
                                        "email": "saurabh@example.com",
                                        "role": "CANDIDATE"
                                    }
                                    """));
  }

  @Test
  void shouldRejectInvalidEmail() throws Exception {

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "email": "invalid-email",
                                        "password": "mysecretpassword"
                                    }
                                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectShortPassword() throws Exception {

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "email": "saurabh@example.com",
                                        "password": "123"
                                    }
                                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectMissingEmail() throws Exception {

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "password": "mysecretpassword"
                                    }
                                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectDuplicateEmail() throws Exception {

    when(userService.register(any())).thenThrow(new DataConflictException("Email already exists"));

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "email": "saurabh@example.com",
                                        "password": "mysecretpassword"
                                    }
                                    """))
        .andExpect(status().isConflict())
        .andExpect(content().string("Email already exists"));
  }

  @Test
  void shouldLoginUser() throws Exception {

    LoginResponse response =
        new LoginResponse("test.jwt.token", 1L, "saurabh@example.com", "CANDIDATE");

    when(userService.login(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "email": "saurabh@example.com",
                                        "password": "mysecretpassword"
                                    }
                                    """))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    """
                                    {
                                        "token": "test.jwt.token",
                                        "id": 1,
                                        "email": "saurabh@example.com",
                                        "role": "CANDIDATE"
                                    }
                                    """));
  }

  @Test
  void shouldRejectWrongPassword() throws Exception {

    when(userService.login(any()))
        .thenThrow(new AuthenticationException("Invalid email or password"));

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "email": "saurabh@example.com",
                                        "password": "wrongpassword"
                                    }
                                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid email or password"));
  }

  @Test
  void shouldRejectUnknownEmail() throws Exception {

    when(userService.login(any()))
        .thenThrow(new AuthenticationException("Invalid email or password"));

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "email": "unknown@example.com",
                                        "password": "mysecretpassword"
                                    }
                                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid email or password"));
  }
}
