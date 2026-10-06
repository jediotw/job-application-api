package com.example.jobapplicationapi.candidate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jobapplicationapi.application.repository.ApplicationRepository;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class CandidateIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CandidateRepository candidateRepository;

  @Autowired private ApplicationRepository applicationRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private String token;

  @BeforeEach
  void cleanDatabase() throws Exception {

    applicationRepository.deleteAll();
    candidateRepository.deleteAll();
    userRepository.deleteAll();

    User user = new User();

    user.setEmail("test@example.com");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    user.setRole("CANDIDATE");

    userRepository.save(user);

    String loginRequest =
        """
            {
                "email": "test@example.com",
                "password": "password123"
            }
            """;

    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequest))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode responseJson = objectMapper.readTree(responseBody);

    token = responseJson.get("token").asText();
  }

  @Test
  void shouldGetAllCandidates() throws Exception {

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");
    candidate.setPhone("9876543210");
    candidate.setResumeUrl("https://example.com/resume");

    candidateRepository.save(candidate);

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Saurabh Kumar"))
        .andExpect(jsonPath("$[0].email").value("saurabh@example.com"));
  }

  @Test
  void shouldGetCandidateById() throws Exception {

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");
    candidate.setPhone("9876543210");
    candidate.setResumeUrl("https://example.com/resume");

    Candidate savedCandidate = candidateRepository.save(candidate);

    mockMvc
        .perform(
            get("/candidates/" + savedCandidate.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedCandidate.getId()))
        .andExpect(jsonPath("$.name").value("Saurabh Kumar"))
        .andExpect(jsonPath("$.email").value("saurabh@example.com"));
  }

  @Test
  void shouldReturn404WhenCandidateDoesNotExist() throws Exception {

    mockMvc
        .perform(get("/candidates/999999").header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldCreateCandidate() throws Exception {

    String requestJson =
        """
            {
                "name": "Saurabh Kumar",
                "email": "saurabh@example.com",
                "phone": "9876543210",
                "resumeUrl": "https://example.com/resume"
            }
            """;

    mockMvc
        .perform(
            post("/candidates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Saurabh Kumar"))
        .andExpect(jsonPath("$.email").value("saurabh@example.com"));
  }

  @Test
  void shouldReturn400WhenCreatingInvalidCandidate() throws Exception {

    String requestJson =
        """
            {
                "name": "",
                "email": "invalid-email",
                "phone": "9876543210"
            }
            """;

    mockMvc
        .perform(
            post("/candidates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn409WhenEmailAlreadyExists() throws Exception {

    Candidate candidate = new Candidate();

    candidate.setName("Existing Candidate");
    candidate.setEmail("existing@example.com");
    candidate.setPhone("9876543210");

    candidateRepository.save(candidate);

    String requestJson =
        """
            {
                "name": "New Candidate",
                "email": "existing@example.com",
                "phone": "9999999999"
            }
            """;

    mockMvc
        .perform(
            post("/candidates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldUpdateCandidate() throws Exception {

    Candidate candidate = new Candidate();

    candidate.setName("Old Name");
    candidate.setEmail("old@example.com");
    candidate.setPhone("9876543210");

    Candidate savedCandidate = candidateRepository.save(candidate);

    String requestJson =
        """
            {
                "name": "New Name",
                "email": "new@example.com",
                "phone": "9999999999",
                "resumeUrl": "https://example.com/new-resume"
            }
            """;

    mockMvc
        .perform(
            put("/candidates/" + savedCandidate.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New Name"))
        .andExpect(jsonPath("$.email").value("new@example.com"))
        .andExpect(jsonPath("$.phone").value("9999999999"));
  }

  @Test
  void shouldReturn404WhenUpdatingNonExistingCandidate() throws Exception {

    String requestJson =
        """
            {
                "name": "New Name",
                "email": "new@example.com",
                "phone": "9999999999",
                "resumeUrl": "https://example.com/resume"
            }
            """;

    mockMvc
        .perform(
            put("/candidates/999999")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenUpdatingInvalidCandidate() throws Exception {

    Candidate candidate = new Candidate();

    candidate.setName("Old Name");
    candidate.setEmail("old@example.com");
    candidate.setPhone("9876543210");

    Candidate savedCandidate = candidateRepository.save(candidate);

    String requestJson =
        """
            {
                "name": "",
                "email": "invalid-email",
                "phone": "9999999999"
            }
            """;

    mockMvc
        .perform(
            put("/candidates/" + savedCandidate.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn409WhenUpdatingWithExistingEmail() throws Exception {

    Candidate firstCandidate = new Candidate();

    firstCandidate.setName("First Candidate");
    firstCandidate.setEmail("first@example.com");
    firstCandidate.setPhone("1111111111");

    candidateRepository.save(firstCandidate);

    Candidate secondCandidate = new Candidate();

    secondCandidate.setName("Second Candidate");
    secondCandidate.setEmail("second@example.com");
    secondCandidate.setPhone("2222222222");

    Candidate savedSecondCandidate = candidateRepository.save(secondCandidate);

    String requestJson =
        """
            {
                "name": "Second Candidate",
                "email": "first@example.com",
                "phone": "3333333333"
            }
            """;

    mockMvc
        .perform(
            put("/candidates/" + savedSecondCandidate.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldDeleteCandidate() throws Exception {

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");
    candidate.setPhone("9876543210");

    Candidate savedCandidate = candidateRepository.save(candidate);

    mockMvc
        .perform(
            delete("/candidates/" + savedCandidate.getId())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    Optional<Candidate> deletedCandidate = candidateRepository.findById(savedCandidate.getId());

    org.junit.jupiter.api.Assertions.assertTrue(deletedCandidate.isEmpty());
  }

  @Test
  void shouldReturn404WhenDeletingNonExistingCandidate() throws Exception {

    mockMvc
        .perform(delete("/candidates/999999").header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
  }
}
