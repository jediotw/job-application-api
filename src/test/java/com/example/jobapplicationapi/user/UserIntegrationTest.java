package com.example.jobapplicationapi.user;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jobapplicationapi.application.repository.ApplicationRepository;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.company.repository.CompanyRepository;
import com.example.jobapplicationapi.job.repository.JobRepository;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ApplicationRepository applicationRepository;

  @Autowired private CandidateRepository candidateRepository;

  @Autowired private CompanyRepository companyRepository;

  @Autowired private JobRepository jobRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Value("${jwt.secret}") private String jwtSecret;

  @BeforeEach
  void cleanDatabase() {
    applicationRepository.deleteAll();
    candidateRepository.deleteAll();
    jobRepository.deleteAll();
    companyRepository.deleteAll();
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

  private User saveUser(String email, String password, String role) {

    User user = new User();

    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole(role);

    return userRepository.save(user);
  }

  private String login(String email, String password) throws Exception {

    String loginRequest = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequest))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode responseJson = objectMapper.readTree(responseBody);

    return responseJson.get("token").asText();
  }

  @Test
  void shouldReturn401WhenTokenIsMalformed() throws Exception {

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer not-a-jwt"))
        .andExpect(status().isUnauthorized());

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer a.b.c"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401WhenTokenEmailDoesNotExist() throws Exception {

    saveUser("ghost@example.com", "mysecretpassword", "CANDIDATE");

    String token = login("ghost@example.com", "mysecretpassword");

    userRepository.deleteAll();

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401WhenTokenIsExpired() throws Exception {

    saveUser("expired@example.com", "mysecretpassword", "CANDIDATE");

    SecretKey secretKey =
        Keys.hmacShaKeyFor(
            jwtSecret.getBytes(StandardCharsets.UTF_8));

    String expiredToken =
        Jwts.builder()
            .subject("expired@example.com")
            .issuedAt(new Date(System.currentTimeMillis() - 7200000L))
            .expiration(new Date(System.currentTimeMillis() - 3600000L))
            .signWith(secretKey)
            .compact();

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer " + expiredToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldUseCurrentRoleFromDatabaseInsteadOfTokenRole() throws Exception {

    User user = saveUser("roles@example.com", "mysecretpassword", "CANDIDATE");

    String token = login("roles@example.com", "mysecretpassword");

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    user.setRole("RECRUITER");
    userRepository.save(user);

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }
}
