package com.example.jobapplicationapi.company;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jobapplicationapi.application.repository.ApplicationRepository;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.company.model.Company;
import com.example.jobapplicationapi.company.repository.CompanyRepository;
import com.example.jobapplicationapi.job.repository.JobRepository;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
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
public class CompanyIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ApplicationRepository applicationRepository;

  @Autowired private CandidateRepository candidateRepository;

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

    currentUser = createUser("recruiter@example.com", "password123", "RECRUITER");

    token = login("recruiter@example.com", "password123");
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

  private Company createCompany(Long recruiterId, String name, String cin) {

    Company company = new Company();

    company.setRecruiterId(recruiterId);
    company.setName(name);
    company.setCin(cin);
    company.setWebsite("https://example.com");
    company.setDescription("Technology company");

    return companyRepository.save(company);
  }

  private String companyJson(String name, String cin) {

    return String.format(
        "{\"name\":\"%s\",\"cin\":\"%s\",\"website\":\"https://example.com\","
            + "\"description\":\"Technology company\"}",
        name, cin);
  }

  @Test
  void shouldCreateCompany() throws Exception {

    mockMvc
        .perform(
            post("/companies")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyJson("Google", "L12345DL202012345")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Google"));

    Iterator<Company> iterator = companyRepository.findAll().iterator();

    org.junit.jupiter.api.Assertions.assertTrue(iterator.hasNext());

    Company savedCompany = iterator.next();

    org.junit.jupiter.api.Assertions.assertEquals(
        currentUser.getId(), savedCompany.getRecruiterId());
  }

  @Test
  void shouldUpdateOwnCompany() throws Exception {

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    mockMvc
        .perform(
            put("/companies/" + ownCompany.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyJson("Google Updated", "L12345DL202012346")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Google Updated"));
  }

  @Test
  void shouldDeleteOwnCompany() throws Exception {

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    mockMvc
        .perform(
            delete("/companies/" + ownCompany.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    org.junit.jupiter.api.Assertions.assertTrue(
        companyRepository.findById(ownCompany.getId()).isEmpty());
  }

  @Test
  void shouldReturn403WhenUpdatingAnotherRecruitersCompany() throws Exception {

    User otherRecruiter = createUser("other.recruiter@example.com", "password123", "RECRUITER");

    Company otherCompany = createCompany(otherRecruiter.getId(), "Meta", "L12345DL202012346");

    mockMvc
        .perform(
            put("/companies/" + otherCompany.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyJson("Hacked Meta", "L12345DL202012347")))
        .andExpect(status().isForbidden());

    Company unchangedCompany = companyRepository.findById(otherCompany.getId()).orElseThrow();

    org.junit.jupiter.api.Assertions.assertEquals("Meta", unchangedCompany.getName());
  }

  @Test
  void shouldReturn403WhenDeletingAnotherRecruitersCompany() throws Exception {

    User otherRecruiter = createUser("other.recruiter@example.com", "password123", "RECRUITER");

    Company otherCompany = createCompany(otherRecruiter.getId(), "Meta", "L12345DL202012346");

    mockMvc
        .perform(
            delete("/companies/" + otherCompany.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertTrue(
        companyRepository.findById(otherCompany.getId()).isPresent());
  }

  @Test
  void shouldAllowCandidateToReadCompanies() throws Exception {

    Company savedCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    createUser("candidate@example.com", "password123", "CANDIDATE");

    String candidateToken = login("candidate@example.com", "password123");

    mockMvc
        .perform(get("/companies").header("Authorization", "Bearer " + candidateToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(savedCompany.getId()));

    mockMvc
        .perform(
            get("/companies/" + savedCompany.getId())
                .header("Authorization", "Bearer " + candidateToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedCompany.getId()));
  }

  @Test
  void shouldReturn403WhenCandidateCreatesCompany() throws Exception {

    createUser("candidate@example.com", "password123", "CANDIDATE");

    String candidateToken = login("candidate@example.com", "password123");

    mockMvc
        .perform(
            post("/companies")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyJson("Google", "L12345DL202012345")))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertFalse(companyRepository.findAll().iterator().hasNext());
  }

  @Test
  void shouldReturn403WhenCandidateUpdatesCompany() throws Exception {

    Company savedCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    createUser("candidate@example.com", "password123", "CANDIDATE");

    String candidateToken = login("candidate@example.com", "password123");

    mockMvc
        .perform(
            put("/companies/" + savedCompany.getId())
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyJson("Hacked Google", "L12345DL202012346")))
        .andExpect(status().isForbidden());

    Company unchangedCompany = companyRepository.findById(savedCompany.getId()).orElseThrow();

    org.junit.jupiter.api.Assertions.assertEquals("Google", unchangedCompany.getName());
  }

  @Test
  void shouldReturn403WhenCandidateDeletesCompany() throws Exception {

    Company savedCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    createUser("candidate@example.com", "password123", "CANDIDATE");

    String candidateToken = login("candidate@example.com", "password123");

    mockMvc
        .perform(
            delete("/companies/" + savedCompany.getId())
                .header("Authorization", "Bearer " + candidateToken))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertTrue(
        companyRepository.findById(savedCompany.getId()).isPresent());
  }

  @Test
  void shouldReturn401WhenNotAuthenticated() throws Exception {

    mockMvc.perform(get("/companies")).andExpect(status().isUnauthorized());

    mockMvc
        .perform(
            post("/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyJson("Google", "L12345DL202012345")))
        .andExpect(status().isUnauthorized());
  }
}
