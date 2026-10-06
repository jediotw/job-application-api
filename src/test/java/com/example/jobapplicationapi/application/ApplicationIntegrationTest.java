package com.example.jobapplicationapi.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jobapplicationapi.application.model.Application;
import com.example.jobapplicationapi.application.repository.ApplicationRepository;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.company.model.Company;
import com.example.jobapplicationapi.company.repository.CompanyRepository;
import com.example.jobapplicationapi.job.model.Job;
import com.example.jobapplicationapi.job.repository.JobRepository;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
public class ApplicationIntegrationTest {

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

  private Candidate createCandidate(Long userId, String name, String email) {

    Candidate candidate = new Candidate();

    candidate.setUserId(userId);
    candidate.setName(name);
    candidate.setEmail(email);
    candidate.setPhone("9876543210");
    candidate.setResumeUrl("https://example.com/resume");

    return candidateRepository.save(candidate);
  }

  private Application createApplication(Long candidateId, Long jobId, String status) {

    Application application = new Application();

    application.setCandidateId(candidateId);
    application.setJobId(jobId);
    application.setStatus(status);
    application.setAppliedAt(LocalDateTime.now());

    return applicationRepository.save(application);
  }

  @Test
  void shouldCreateApplication() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    String requestBody =
        "{" + "\"jobId\":" + savedJob.getId() + "," + "\"status\":\"APPLIED\"" + "}";

    mockMvc
        .perform(
            post("/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.candidateId").value(savedCandidate.getId()))
        .andExpect(jsonPath("$.jobId").value(savedJob.getId()))
        .andExpect(jsonPath("$.status").value("APPLIED"));
  }

  @Test
  void shouldDeriveCandidateIdFromAuthenticatedUser() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    User otherUser = createUser("other@example.com", "password123", "CANDIDATE");

    Candidate otherCandidate =
        createCandidate(otherUser.getId(), "Other Candidate", "other@example.com");

    String requestBody =
        "{"
            + "\"candidateId\":"
            + otherCandidate.getId()
            + ","
            + "\"jobId\":"
            + savedJob.getId()
            + ","
            + "\"status\":\"APPLIED\""
            + "}";

    mockMvc
        .perform(
            post("/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.candidateId").value(savedCandidate.getId()))
        .andExpect(jsonPath("$.jobId").value(savedJob.getId()));
  }

  @Test
  void shouldReturn404WhenCandidateDoesNotExist() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    String requestBody =
        "{" + "\"jobId\":" + savedJob.getId() + "," + "\"status\":\"APPLIED\"" + "}";

    mockMvc
        .perform(
            post("/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenJobDoesNotExist() throws Exception {

    createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    String requestBody = "{" + "\"jobId\":99999," + "\"status\":\"APPLIED\"" + "}";

    mockMvc
        .perform(
            post("/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldGetAllApplications() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    mockMvc
        .perform(get("/applications").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(savedApplication.getId()))
        .andExpect(jsonPath("$[0].candidateId").value(savedCandidate.getId()))
        .andExpect(jsonPath("$[0].jobId").value(savedJob.getId()))
        .andExpect(jsonPath("$[0].status").value("APPLIED"));
  }

  @Test
  void shouldGetApplicationById() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    mockMvc
        .perform(
            get("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedApplication.getId()))
        .andExpect(jsonPath("$.candidateId").value(savedCandidate.getId()))
        .andExpect(jsonPath("$.jobId").value(savedJob.getId()))
        .andExpect(jsonPath("$.status").value("APPLIED"));
  }

  @Test
  void shouldReturn404WhenApplicationDoesNotExist() throws Exception {

    mockMvc
        .perform(get("/applications/99999").header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldUpdateApplication() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    String requestBody = "{\"status\":\"INTERVIEW\"}";

    mockMvc
        .perform(
            put("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedApplication.getId()))
        .andExpect(jsonPath("$.status").value("INTERVIEW"));
  }

  @Test
  void shouldReturn404WhenUpdatingMissingApplication() throws Exception {

    String requestBody = "{\"status\":\"INTERVIEW\"}";

    mockMvc
        .perform(
            put("/applications/99999")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteApplication() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    mockMvc
        .perform(
            delete("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn409WhenApplicationAlreadyExists() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    String requestBody =
        "{" + "\"jobId\":" + savedJob.getId() + "," + "\"status\":\"APPLIED\"" + "}";

    mockMvc
        .perform(
            post("/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldReturn403WhenReadingAnotherCandidatesApplication() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    User otherUser = createUser("other@example.com", "password123", "CANDIDATE");

    Candidate otherCandidate =
        createCandidate(otherUser.getId(), "Other Candidate", "other@example.com");

    Application otherApplication =
        createApplication(otherCandidate.getId(), savedJob.getId(), "APPLIED");

    mockMvc
        .perform(
            get("/applications/" + otherApplication.getId())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(get("/applications").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void shouldReturn403WhenUpdatingAnotherCandidatesApplication() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    User otherUser = createUser("other@example.com", "password123", "CANDIDATE");

    Candidate otherCandidate =
        createCandidate(otherUser.getId(), "Other Candidate", "other@example.com");

    Application otherApplication =
        createApplication(otherCandidate.getId(), savedJob.getId(), "APPLIED");

    String requestBody = "{\"status\":\"INTERVIEW\"}";

    mockMvc
        .perform(
            put("/applications/" + otherApplication.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isForbidden());

    Application unchangedApplication =
        applicationRepository.findById(otherApplication.getId()).orElseThrow();

    org.junit.jupiter.api.Assertions.assertEquals("APPLIED", unchangedApplication.getStatus());
  }

  @Test
  void shouldReturn403WhenDeletingAnotherCandidatesApplication() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    User otherUser = createUser("other@example.com", "password123", "CANDIDATE");

    Candidate otherCandidate =
        createCandidate(otherUser.getId(), "Other Candidate", "other@example.com");

    Application otherApplication =
        createApplication(otherCandidate.getId(), savedJob.getId(), "APPLIED");

    mockMvc
        .perform(
            delete("/applications/" + otherApplication.getId())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertTrue(
        applicationRepository.findById(otherApplication.getId()).isPresent());
  }

  @Test
  void shouldAllowRecruiterToReadApplicationsForOwnCompany() throws Exception {

    User recruiter = createUser("recruiter@example.com", "password123", "RECRUITER");

    Company savedCompany = createCompany(recruiter.getId(), "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    String recruiterToken = login("recruiter@example.com", "password123");

    mockMvc
        .perform(get("/applications").header("Authorization", "Bearer " + recruiterToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(savedApplication.getId()));

    mockMvc
        .perform(
            get("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + recruiterToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedApplication.getId()));
  }

  @Test
  void shouldReturn403WhenRecruiterReadsApplicationForAnotherRecruitersCompany() throws Exception {

    User otherRecruiter = createUser("other.recruiter@example.com", "password123", "RECRUITER");

    Company savedCompany = createCompany(otherRecruiter.getId(), "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    createUser("recruiter@example.com", "password123", "RECRUITER");

    String recruiterToken = login("recruiter@example.com", "password123");

    mockMvc
        .perform(
            get("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + recruiterToken))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(get("/applications").header("Authorization", "Bearer " + recruiterToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void shouldAllowRecruiterToUpdateApplicationForOwnCompany() throws Exception {

    User recruiter = createUser("recruiter@example.com", "password123", "RECRUITER");

    Company savedCompany = createCompany(recruiter.getId(), "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    String recruiterToken = login("recruiter@example.com", "password123");

    String requestBody = "{\"status\":\"HIRED\"}";

    mockMvc
        .perform(
            put("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("HIRED"));
  }

  @Test
  void shouldReturn403WhenRecruiterUpdatesApplicationForAnotherRecruitersCompany()
      throws Exception {

    User otherRecruiter = createUser("other.recruiter@example.com", "password123", "RECRUITER");

    Company savedCompany = createCompany(otherRecruiter.getId(), "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    createUser("recruiter@example.com", "password123", "RECRUITER");

    String recruiterToken = login("recruiter@example.com", "password123");

    String requestBody = "{\"status\":\"HIRED\"}";

    mockMvc
        .perform(
            put("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isForbidden());

    Application unchangedApplication =
        applicationRepository.findById(savedApplication.getId()).orElseThrow();

    org.junit.jupiter.api.Assertions.assertEquals("APPLIED", unchangedApplication.getStatus());
  }

  @Test
  void shouldReturn403WhenRecruiterDeletesApplicationForAnotherRecruitersCompany()
      throws Exception {

    User otherRecruiter = createUser("other.recruiter@example.com", "password123", "RECRUITER");

    Company savedCompany = createCompany(otherRecruiter.getId(), "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    createUser("recruiter@example.com", "password123", "RECRUITER");

    String recruiterToken = login("recruiter@example.com", "password123");

    mockMvc
        .perform(
            delete("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + recruiterToken))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertTrue(
        applicationRepository.findById(savedApplication.getId()).isPresent());
  }

  @Test
  void shouldAllowRecruiterToDeleteApplicationForOwnCompany() throws Exception {

    User recruiter = createUser("recruiter@example.com", "password123", "RECRUITER");

    Company savedCompany = createCompany(recruiter.getId(), "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    Candidate savedCandidate =
        createCandidate(currentUser.getId(), "Saurabh Kumar", "saurabh@example.com");

    Application savedApplication =
        createApplication(savedCandidate.getId(), savedJob.getId(), "APPLIED");

    String recruiterToken = login("recruiter@example.com", "password123");

    mockMvc
        .perform(
            delete("/applications/" + savedApplication.getId())
                .header("Authorization", "Bearer " + recruiterToken))
        .andExpect(status().isOk());

    org.junit.jupiter.api.Assertions.assertTrue(
        applicationRepository.findById(savedApplication.getId()).isEmpty());
  }

  @Test
  void shouldReturn403WhenRecruiterCreatesApplication() throws Exception {

    Company savedCompany = createCompany(null, "Google", "L12345DL202012345");

    Job savedJob = createJob(savedCompany.getId(), "Backend Engineer");

    createUser("recruiter@example.com", "password123", "RECRUITER");

    String recruiterToken = login("recruiter@example.com", "password123");

    String requestBody = "{\"jobId\":" + savedJob.getId() + ",\"status\":\"APPLIED\"}";

    mockMvc
        .perform(
            post("/applications")
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldReturn401WhenNotAuthenticated() throws Exception {

    mockMvc.perform(get("/applications")).andExpect(status().isUnauthorized());

    mockMvc
        .perform(get("/applications").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());
  }
}
