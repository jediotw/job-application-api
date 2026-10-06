package com.example.jobapplicationapi.job;

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
import com.example.jobapplicationapi.job.model.Job;
import com.example.jobapplicationapi.job.repository.JobRepository;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class JobIntegrationTest {

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

  private Job createJob(Long companyId, String title) {

    Job job = new Job();

    job.setCompanyId(companyId);
    job.setTitle(title);
    job.setDescription("Build backend services");
    job.setLocation("Bangalore");
    job.setEmploymentType("FULL_TIME");

    return jobRepository.save(job);
  }

  private String jobJson(Long companyId, String title) {

    return String.format(
        "{\"companyId\":%d,\"title\":\"%s\",\"description\":\"Build backend services\","
            + "\"location\":\"Bangalore\",\"employmentType\":\"FULL_TIME\"}",
        companyId, title);
  }

  private boolean anyJobExists() {
    return jobRepository.findAll().iterator().hasNext();
  }

  @Test
  void shouldCreateJobForOwnCompany() throws Exception {

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    mockMvc
        .perform(
            post("/jobs")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jobJson(ownCompany.getId(), "Backend Engineer")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Backend Engineer"))
        .andExpect(jsonPath("$.companyId").value(ownCompany.getId()));
  }

  @Test
  void shouldReturn403WhenCreatingJobForAnotherRecruitersCompany() throws Exception {

    User otherRecruiter = createUser("other.recruiter@example.com", "password123", "RECRUITER");

    Company otherCompany = createCompany(otherRecruiter.getId(), "Meta", "L12345DL202012346");

    mockMvc
        .perform(
            post("/jobs")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jobJson(otherCompany.getId(), "Backend Engineer")))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertFalse(anyJobExists());
  }

  @Test
  void shouldUpdateOwnCompanysJob() throws Exception {

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    Job ownJob = createJob(ownCompany.getId(), "Backend Engineer");

    mockMvc
        .perform(
            put("/jobs/" + ownJob.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jobJson(ownCompany.getId(), "Senior Backend Engineer")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Senior Backend Engineer"));
  }

  @Test
  void shouldReturn403WhenUpdatingAnotherCompanysJob() throws Exception {

    User otherRecruiter = createUser("other.recruiter@example.com", "password123", "RECRUITER");

    Company otherCompany = createCompany(otherRecruiter.getId(), "Meta", "L12345DL202012346");

    Job otherJob = createJob(otherCompany.getId(), "Backend Engineer");

    mockMvc
        .perform(
            put("/jobs/" + otherJob.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jobJson(otherCompany.getId(), "Hacked Job")))
        .andExpect(status().isForbidden());

    Job unchangedJob = jobRepository.findById(otherJob.getId()).orElseThrow();

    org.junit.jupiter.api.Assertions.assertEquals("Backend Engineer", unchangedJob.getTitle());
  }

  @Test
  void shouldReturn403WhenMovingAnotherRecruitersJobToOwnCompany() throws Exception {

    User otherRecruiter = createUser("other.recruiter@example.com", "password123", "RECRUITER");

    Company otherCompany = createCompany(otherRecruiter.getId(), "Meta", "L12345DL202012346");

    Job otherJob = createJob(otherCompany.getId(), "Backend Engineer");

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    mockMvc
        .perform(
            put("/jobs/" + otherJob.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jobJson(ownCompany.getId(), "Stolen Job")))
        .andExpect(status().isForbidden());

    Job unchangedJob = jobRepository.findById(otherJob.getId()).orElseThrow();

    org.junit.jupiter.api.Assertions.assertEquals(
        otherCompany.getId(), unchangedJob.getCompanyId());
    org.junit.jupiter.api.Assertions.assertEquals("Backend Engineer", unchangedJob.getTitle());
  }

  @Test
  void shouldDeleteOwnCompanysJob() throws Exception {

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    Job ownJob = createJob(ownCompany.getId(), "Backend Engineer");

    mockMvc
        .perform(delete("/jobs/" + ownJob.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    org.junit.jupiter.api.Assertions.assertTrue(jobRepository.findById(ownJob.getId()).isEmpty());
  }

  @Test
  void shouldReturn403WhenDeletingAnotherCompanysJob() throws Exception {

    User otherRecruiter = createUser("other.recruiter@example.com", "password123", "RECRUITER");

    Company otherCompany = createCompany(otherRecruiter.getId(), "Meta", "L12345DL202012346");

    Job otherJob = createJob(otherCompany.getId(), "Backend Engineer");

    mockMvc
        .perform(delete("/jobs/" + otherJob.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertTrue(
        jobRepository.findById(otherJob.getId()).isPresent());
  }

  @Test
  void shouldAllowCandidateToReadJobs() throws Exception {

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    Job savedJob = createJob(ownCompany.getId(), "Backend Engineer");

    createUser("candidate@example.com", "password123", "CANDIDATE");

    String candidateToken = login("candidate@example.com", "password123");

    mockMvc
        .perform(get("/jobs").header("Authorization", "Bearer " + candidateToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(savedJob.getId()));

    mockMvc
        .perform(
            get("/jobs/" + savedJob.getId()).header("Authorization", "Bearer " + candidateToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedJob.getId()));
  }

  @Test
  void shouldReturn403WhenCandidateCreatesJob() throws Exception {

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    createUser("candidate@example.com", "password123", "CANDIDATE");

    String candidateToken = login("candidate@example.com", "password123");

    mockMvc
        .perform(
            post("/jobs")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jobJson(ownCompany.getId(), "Backend Engineer")))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertFalse(anyJobExists());
  }

  @Test
  void shouldReturn403WhenCandidateUpdatesJob() throws Exception {

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    Job savedJob = createJob(ownCompany.getId(), "Backend Engineer");

    createUser("candidate@example.com", "password123", "CANDIDATE");

    String candidateToken = login("candidate@example.com", "password123");

    mockMvc
        .perform(
            put("/jobs/" + savedJob.getId())
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jobJson(ownCompany.getId(), "Hacked Job")))
        .andExpect(status().isForbidden());

    Job unchangedJob = jobRepository.findById(savedJob.getId()).orElseThrow();

    org.junit.jupiter.api.Assertions.assertEquals("Backend Engineer", unchangedJob.getTitle());
  }

  @Test
  void shouldReturn403WhenCandidateDeletesJob() throws Exception {

    Company ownCompany = createCompany(currentUser.getId(), "Google", "L12345DL202012345");

    Job savedJob = createJob(ownCompany.getId(), "Backend Engineer");

    createUser("candidate@example.com", "password123", "CANDIDATE");

    String candidateToken = login("candidate@example.com", "password123");

    mockMvc
        .perform(
            delete("/jobs/" + savedJob.getId()).header("Authorization", "Bearer " + candidateToken))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertTrue(
        jobRepository.findById(savedJob.getId()).isPresent());
  }

  @Test
  void shouldReturn401WhenNotAuthenticated() throws Exception {

    mockMvc.perform(get("/jobs")).andExpect(status().isUnauthorized());

    mockMvc
        .perform(
            post("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jobJson(1L, "Backend Engineer")))
        .andExpect(status().isUnauthorized());
  }
}
