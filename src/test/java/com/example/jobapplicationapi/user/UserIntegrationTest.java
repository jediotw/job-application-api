package com.example.jobapplicationapi.user;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void cleanDatabase() {
    userRepository.deleteAll();
  }

  @Test
  void shouldRegisterUser() throws Exception {

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
        .andExpect(status().isOk());
  }

  @Test
  void shouldRejectDuplicateEmail() throws Exception {

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
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "email": "saurabh@example.com",
                                        "password": "anotherpassword"
                                    }
                                    """))
        .andExpect(status().isConflict());
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
  void shouldStoreHashedPassword() throws Exception {

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                        "email": "hash@example.com",
                                        "password": "mysecretpassword"
                                    }
                                    """))
        .andExpect(status().isOk());

    User user = userRepository.findByEmail("hash@example.com").orElseThrow();

    assertNotEquals("mysecretpassword", user.getPasswordHash());

    assertTrue(user.getPasswordHash().startsWith("$2a$"));
  }

  @Test
  void shouldLoginUser() throws Exception {

    User user = new User();

    user.setEmail("saurabh@example.com");
    user.setPasswordHash(passwordEncoder.encode("mysecretpassword"));
    user.setRole("CANDIDATE");

    userRepository.save(user);

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
        .andExpect(status().isOk());
  }

  @Test
  void shouldRejectWrongPassword() throws Exception {

    User user = new User();

    user.setEmail("saurabh@example.com");
    user.setPasswordHash(passwordEncoder.encode("mysecretpassword"));
    user.setRole("CANDIDATE");

    userRepository.save(user);

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
