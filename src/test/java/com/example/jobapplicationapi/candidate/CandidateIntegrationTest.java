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
import com.example.jobapplicationapi.company.repository.CompanyRepository;
import com.example.jobapplicationapi.job.repository.JobRepository;
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

  @Autowired private CompanyRepository companyRepository;

  @Autowired private JobRepository jobRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private User currentUser;

  private String token;

  @BeforeEach
  void cleanDatabase() throws Exception {

    applicationRepository.deleteAll();
    candidateRepository.deleteAll();
    jobRepository.deleteAll();
    companyRepository.deleteAll();
    userRepository.deleteAll();

    currentUser = createUser("test@example.com", "password123", "CANDIDATE");

    token = login("test@example.com", "password123");
  }

  private User createUser(String email, String password, String role) {

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

  private Candidate createCandidate(Long userId, String name, String email) {

    Candidate candidate = new Candidate();

    candidate.setUserId(userId);
    candidate.setName(name);
    candidate.setEmail(email);
    candidate.setPhone("9876543210");
    candidate.setResumeUrl("https://example.com/resume");

    return candidateRepository.save(candidate);
  }

  @Test
  void shouldGetAllCandidates() throws Exception {

    Candidate candidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    User otherUser = createUser("other@example.com", "password123", "CANDIDATE");

    createCandidate(otherUser.getId(), "Other Candidate", "other@example.com");

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(candidate.getId()))
        .andExpect(jsonPath("$[0].name").value("Saurabh Kumar"))
        .andExpect(jsonPath("$[0].email").value("saurabh@example.com"));
  }

  @Test
  void shouldGetCandidateById() throws Exception {

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

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

    Optional<Candidate> savedCandidate = candidateRepository.findByUserId(currentUser.getId());

    org.junit.jupiter.api.Assertions.assertTrue(savedCandidate.isPresent());
    org.junit.jupiter.api.Assertions.assertEquals(
        "saurabh@example.com", savedCandidate.get().getEmail());
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

    createCandidate(currentUser.getId(), "Existing Candidate", "existing@example.com");

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

    Candidate savedCandidate = createCandidate(currentUser.getId(), "Old Name", "old@example.com");

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

    Candidate savedCandidate = createCandidate(currentUser.getId(), "Old Name", "old@example.com");

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

    createCandidate(currentUser.getId(), "First Candidate", "first@example.com");

    Candidate savedSecondCandidate =
        createCandidate(currentUser.getId(), "Second Candidate", "second@example.com");

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

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

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

  @Test
  void shouldReturn403WhenReadingAnotherCandidatesProfile() throws Exception {

    User otherUser = createUser("other@example.com", "password123", "CANDIDATE");

    Candidate otherCandidate =
        createCandidate(otherUser.getId(), "Other Candidate", "other@example.com");

    mockMvc
        .perform(
            get("/candidates/" + otherCandidate.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldReturn403WhenUpdatingAnotherCandidatesProfile() throws Exception {

    User otherUser = createUser("other@example.com", "password123", "CANDIDATE");

    Candidate otherCandidate =
        createCandidate(otherUser.getId(), "Other Candidate", "other@example.com");

    String requestJson =
        """
            {
                "name": "Hacked Name",
                "email": "hacked@example.com",
                "phone": "9999999999",
                "resumeUrl": "https://example.com/hacked"
            }
            """;

    mockMvc
        .perform(
            put("/candidates/" + otherCandidate.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isForbidden());

    Candidate unchangedCandidate =
        candidateRepository.findById(otherCandidate.getId()).orElseThrow();

    org.junit.jupiter.api.Assertions.assertEquals("Other Candidate", unchangedCandidate.getName());
  }

  @Test
  void shouldReturn403WhenDeletingAnotherCandidatesProfile() throws Exception {

    User otherUser = createUser("other@example.com", "password123", "CANDIDATE");

    Candidate otherCandidate =
        createCandidate(otherUser.getId(), "Other Candidate", "other@example.com");

    mockMvc
        .perform(
            delete("/candidates/" + otherCandidate.getId())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertTrue(
        candidateRepository.findById(otherCandidate.getId()).isPresent());
  }

  @Test
  void shouldReturn401WhenNotAuthenticated() throws Exception {

    mockMvc.perform(get("/candidates")).andExpect(status().isUnauthorized());

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn403WhenRecruiterAccessesCandidateEndpoints() throws Exception {

    createUser("recruiter@example.com", "password123", "RECRUITER");

    String recruiterToken = login("recruiter@example.com", "password123");

    mockMvc
        .perform(get("/candidates").header("Authorization", "Bearer " + recruiterToken))
        .andExpect(status().isForbidden());

    String requestJson =
        """
            {
                "name": "Recruiter Candidate",
                "email": "recruiter.candidate@example.com",
                "phone": "9876543210"
            }
            """;

    mockMvc
        .perform(
            post("/candidates")
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isForbidden());
  }
}
