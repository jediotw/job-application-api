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

  private String token;

  @BeforeEach
  void cleanDatabase() throws Exception {

    applicationRepository.deleteAll();
    candidateRepository.deleteAll();
    jobRepository.deleteAll();
    companyRepository.deleteAll();
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
  void shouldCreateApplication() throws Exception {

    Company company = new Company();

    company.setName("Google");
    company.setCin("L12345DL202012345");
    company.setWebsite("https://google.com");
    company.setDescription("Technology company");

    Company savedCompany = companyRepository.save(company);

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");
    candidate.setPhone("9876543210");
    candidate.setResumeUrl("https://example.com/resume");

    Candidate savedCandidate = candidateRepository.save(candidate);

    Job job = new Job();

    job.setCompanyId(savedCompany.getId());
    job.setTitle("Backend Engineer");
    job.setDescription("Build backend services");
    job.setLocation("Bangalore");
    job.setEmploymentType("FULL_TIME");

    Job savedJob = jobRepository.save(job);

    String requestBody =
        "{"
            + "\"candidateId\":"
            + savedCandidate.getId()
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
        .andExpect(jsonPath("$.jobId").value(savedJob.getId()))
        .andExpect(jsonPath("$.status").value("APPLIED"));
  }

  @Test
  void shouldReturn404WhenCandidateDoesNotExist() throws Exception {

    Company company = new Company();

    company.setName("Google");
    company.setCin("L12345DL202012345");

    Company savedCompany = companyRepository.save(company);

    Job job = new Job();

    job.setCompanyId(savedCompany.getId());
    job.setTitle("Backend Engineer");
    job.setEmploymentType("FULL_TIME");

    Job savedJob = jobRepository.save(job);

    String requestBody =
        "{"
            + "\"candidateId\":99999,"
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
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenJobDoesNotExist() throws Exception {

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");

    Candidate savedCandidate = candidateRepository.save(candidate);

    String requestBody =
        "{"
            + "\"candidateId\":"
            + savedCandidate.getId()
            + ","
            + "\"jobId\":99999,"
            + "\"status\":\"APPLIED\""
            + "}";

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

    Company company = new Company();

    company.setName("Google");
    company.setCin("L12345DL202012345");

    Company savedCompany = companyRepository.save(company);

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");

    Candidate savedCandidate = candidateRepository.save(candidate);

    Job job = new Job();

    job.setCompanyId(savedCompany.getId());
    job.setTitle("Backend Engineer");
    job.setEmploymentType("FULL_TIME");

    Job savedJob = jobRepository.save(job);

    Application application = new Application();

    application.setCandidateId(savedCandidate.getId());

    application.setJobId(savedJob.getId());

    application.setStatus("APPLIED");
    application.setAppliedAt(LocalDateTime.now());

    Application savedApplication = applicationRepository.save(application);

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

    Company company = new Company();

    company.setName("Google");
    company.setCin("L12345DL202012345");

    Company savedCompany = companyRepository.save(company);

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");

    Candidate savedCandidate = candidateRepository.save(candidate);

    Job job = new Job();

    job.setCompanyId(savedCompany.getId());
    job.setTitle("Backend Engineer");
    job.setEmploymentType("FULL_TIME");

    Job savedJob = jobRepository.save(job);

    Application application = new Application();

    application.setCandidateId(savedCandidate.getId());

    application.setJobId(savedJob.getId());

    application.setStatus("APPLIED");

    application.setAppliedAt(LocalDateTime.now());

    Application savedApplication = applicationRepository.save(application);

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

    Company company = new Company();

    company.setName("Google");
    company.setCin("L12345DL202012345");

    Company savedCompany = companyRepository.save(company);

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");

    Candidate savedCandidate = candidateRepository.save(candidate);

    Job job = new Job();

    job.setCompanyId(savedCompany.getId());
    job.setTitle("Backend Engineer");
    job.setEmploymentType("FULL_TIME");

    Job savedJob = jobRepository.save(job);

    Application application = new Application();

    application.setCandidateId(savedCandidate.getId());

    application.setJobId(savedJob.getId());

    application.setStatus("APPLIED");

    application.setAppliedAt(LocalDateTime.now());

    Application savedApplication = applicationRepository.save(application);

    String requestBody =
        "{"
            + "\"candidateId\":"
            + savedCandidate.getId()
            + ","
            + "\"jobId\":"
            + savedJob.getId()
            + ","
            + "\"status\":\"INTERVIEW\""
            + "}";

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

    String requestBody =
        "{" + "\"candidateId\":1," + "\"jobId\":1," + "\"status\":\"INTERVIEW\"" + "}";

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

    Company company = new Company();

    company.setName("Google");
    company.setCin("L12345DL202012345");

    Company savedCompany = companyRepository.save(company);

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");

    Candidate savedCandidate = candidateRepository.save(candidate);

    Job job = new Job();

    job.setCompanyId(savedCompany.getId());
    job.setTitle("Backend Engineer");
    job.setEmploymentType("FULL_TIME");

    Job savedJob = jobRepository.save(job);

    Application application = new Application();

    application.setCandidateId(savedCandidate.getId());

    application.setJobId(savedJob.getId());

    application.setStatus("APPLIED");

    application.setAppliedAt(LocalDateTime.now());

    Application savedApplication = applicationRepository.save(application);

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

    Company company = new Company();

    company.setName("Google");
    company.setCin("L12345DL202012345");

    Company savedCompany = companyRepository.save(company);

    Candidate candidate = new Candidate();

    candidate.setName("Saurabh Kumar");
    candidate.setEmail("saurabh@example.com");

    Candidate savedCandidate = candidateRepository.save(candidate);

    Job job = new Job();

    job.setCompanyId(savedCompany.getId());
    job.setTitle("Backend Engineer");
    job.setEmploymentType("FULL_TIME");

    Job savedJob = jobRepository.save(job);

    Application application = new Application();

    application.setCandidateId(savedCandidate.getId());

    application.setJobId(savedJob.getId());

    application.setStatus("APPLIED");

    application.setAppliedAt(LocalDateTime.now());

    applicationRepository.save(application);

    String requestBody =
        "{"
            + "\"candidateId\":"
            + savedCandidate.getId()
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
        .andExpect(status().isConflict());
  }
}
