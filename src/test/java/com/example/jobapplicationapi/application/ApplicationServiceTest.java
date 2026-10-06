package com.example.jobapplicationapi.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jobapplicationapi.application.dto.CreateApplicationRequest;
import com.example.jobapplicationapi.application.dto.UpdateApplicationRequest;
import com.example.jobapplicationapi.application.model.Application;
import com.example.jobapplicationapi.application.repository.ApplicationRepository;
import com.example.jobapplicationapi.application.service.ApplicationService;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.company.model.Company;
import com.example.jobapplicationapi.company.repository.CompanyRepository;
import com.example.jobapplicationapi.exception.AuthorizationException;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.example.jobapplicationapi.job.model.Job;
import com.example.jobapplicationapi.job.repository.JobRepository;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class ApplicationServiceTest {

  @Test
  void shouldCreateApplication() {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    CandidateRepository candidateRepository = mock(CandidateRepository.class);
    JobRepository jobRepository = mock(JobRepository.class);
    CompanyRepository companyRepository = mock(CompanyRepository.class);
    UserService userService = mock(UserService.class);

    ApplicationService applicationService =
        new ApplicationService(
            applicationRepository,
            candidateRepository,
            jobRepository,
            companyRepository,
            userService);

    User currentUser = new User();
    currentUser.setId(10L);
    currentUser.setEmail("candidate@example.com");
    currentUser.setRole("CANDIDATE");

    when(userService.getCurrentUser()).thenReturn(currentUser);

    Candidate candidate = new Candidate();
    candidate.setId(1L);
    candidate.setUserId(10L);

    when(candidateRepository.findByUserId(10L)).thenReturn(Optional.of(candidate));

    CreateApplicationRequest request = new CreateApplicationRequest();

    request.setJobId(1L);
    request.setStatus("APPLIED");

    Job job = new Job();
    job.setId(1L);

    when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

    Application savedApplication = new Application();

    savedApplication.setId(1L);
    savedApplication.setCandidateId(1L);
    savedApplication.setJobId(1L);
    savedApplication.setStatus("APPLIED");

    when(applicationRepository.save(any(Application.class))).thenReturn(savedApplication);

    Application result = applicationService.createApplication(request);

    assertEquals(1L, result.getId());
    assertEquals(1L, result.getCandidateId());
    assertEquals(1L, result.getJobId());
    assertEquals("APPLIED", result.getStatus());

    verify(userService).getCurrentUser();

    verify(candidateRepository).findByUserId(10L);

    verify(jobRepository).findById(1L);

    verify(applicationRepository).save(any(Application.class));
  }

  @Test
  void shouldThrowExceptionWhenCandidateDoesNotExist() {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    CandidateRepository candidateRepository = mock(CandidateRepository.class);
    JobRepository jobRepository = mock(JobRepository.class);
    CompanyRepository companyRepository = mock(CompanyRepository.class);
    UserService userService = mock(UserService.class);

    ApplicationService applicationService =
        new ApplicationService(
            applicationRepository,
            candidateRepository,
            jobRepository,
            companyRepository,
            userService);

    User currentUser = new User();
    currentUser.setId(10L);
    currentUser.setRole("CANDIDATE");

    when(userService.getCurrentUser()).thenReturn(currentUser);

    when(candidateRepository.findByUserId(10L)).thenReturn(Optional.empty());

    CreateApplicationRequest request = new CreateApplicationRequest();

    request.setJobId(1L);
    request.setStatus("APPLIED");

    assertThrows(
        ResourceNotFoundException.class, () -> applicationService.createApplication(request));

    verify(applicationRepository, never()).save(any(Application.class));
  }

  @Test
  void shouldThrowExceptionWhenJobDoesNotExist() {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    CandidateRepository candidateRepository = mock(CandidateRepository.class);
    JobRepository jobRepository = mock(JobRepository.class);
    CompanyRepository companyRepository = mock(CompanyRepository.class);
    UserService userService = mock(UserService.class);

    ApplicationService applicationService =
        new ApplicationService(
            applicationRepository,
            candidateRepository,
            jobRepository,
            companyRepository,
            userService);

    User currentUser = new User();
    currentUser.setId(10L);
    currentUser.setRole("CANDIDATE");

    when(userService.getCurrentUser()).thenReturn(currentUser);

    Candidate candidate = new Candidate();
    candidate.setId(1L);
    candidate.setUserId(10L);

    when(candidateRepository.findByUserId(10L)).thenReturn(Optional.of(candidate));

    CreateApplicationRequest request = new CreateApplicationRequest();

    request.setJobId(999L);
    request.setStatus("APPLIED");

    when(jobRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> applicationService.createApplication(request));

    verify(applicationRepository, never()).save(any(Application.class));
  }

  @Test
  void shouldGetApplicationById() {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    CandidateRepository candidateRepository = mock(CandidateRepository.class);
    JobRepository jobRepository = mock(JobRepository.class);
    CompanyRepository companyRepository = mock(CompanyRepository.class);
    UserService userService = mock(UserService.class);

    ApplicationService applicationService =
        new ApplicationService(
            applicationRepository,
            candidateRepository,
            jobRepository,
            companyRepository,
            userService);

    Application application = new Application();

    application.setId(1L);
    application.setCandidateId(1L);
    application.setJobId(1L);
    application.setStatus("APPLIED");

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

    User currentUser = new User();
    currentUser.setId(10L);
    currentUser.setEmail("candidate@example.com");
    currentUser.setRole("CANDIDATE");

    when(userService.getCurrentUser()).thenReturn(currentUser);

    Candidate candidate = new Candidate();
    candidate.setId(1L);
    candidate.setUserId(10L);

    when(candidateRepository.findByUserId(10L)).thenReturn(Optional.of(candidate));

    Application result = applicationService.getApplicationById(1L);

    assertEquals(1L, result.getId());
    assertEquals("APPLIED", result.getStatus());

    verify(userService).getCurrentUser();

    verify(candidateRepository).findByUserId(10L);
  }

  @Test
  void shouldThrowExceptionWhenCandidateReadsAnotherCandidatesApplication() {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    CandidateRepository candidateRepository = mock(CandidateRepository.class);
    JobRepository jobRepository = mock(JobRepository.class);
    CompanyRepository companyRepository = mock(CompanyRepository.class);
    UserService userService = mock(UserService.class);

    ApplicationService applicationService =
        new ApplicationService(
            applicationRepository,
            candidateRepository,
            jobRepository,
            companyRepository,
            userService);

    // The application belongs to candidate 2.
    Application application = new Application();

    application.setId(1L);
    application.setCandidateId(2L);
    application.setJobId(1L);
    application.setStatus("APPLIED");

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

    // But the authenticated user owns candidate profile 1.
    User currentUser = new User();
    currentUser.setId(10L);
    currentUser.setEmail("candidate@example.com");
    currentUser.setRole("CANDIDATE");

    when(userService.getCurrentUser()).thenReturn(currentUser);

    Candidate candidate = new Candidate();
    candidate.setId(1L);
    candidate.setUserId(10L);

    when(candidateRepository.findByUserId(10L)).thenReturn(Optional.of(candidate));

    AuthorizationException exception =
        assertThrows(AuthorizationException.class, () -> applicationService.getApplicationById(1L));

    assertEquals("You are not allowed to view this application", exception.getMessage());

    verify(applicationRepository, never()).save(any(Application.class));
  }

  @Test
  void shouldThrowExceptionWhenRecruiterReadsApplicationForAnotherRecruitersCompany() {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    CandidateRepository candidateRepository = mock(CandidateRepository.class);
    JobRepository jobRepository = mock(JobRepository.class);
    CompanyRepository companyRepository = mock(CompanyRepository.class);
    UserService userService = mock(UserService.class);

    ApplicationService applicationService =
        new ApplicationService(
            applicationRepository,
            candidateRepository,
            jobRepository,
            companyRepository,
            userService);

    Application application = new Application();

    application.setId(1L);
    application.setCandidateId(1L);
    application.setJobId(1L);
    application.setStatus("APPLIED");

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

    // The authenticated recruiter is user 10.
    User currentUser = new User();
    currentUser.setId(10L);
    currentUser.setEmail("recruiter@example.com");
    currentUser.setRole("RECRUITER");

    when(userService.getCurrentUser()).thenReturn(currentUser);

    Job job = new Job();
    job.setId(1L);
    job.setCompanyId(1L);

    when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

    // But the company belongs to recruiter 20.
    Company company = new Company();
    company.setId(1L);
    company.setRecruiterId(20L);

    when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

    AuthorizationException exception =
        assertThrows(AuthorizationException.class, () -> applicationService.getApplicationById(1L));

    assertEquals("You are not allowed to view this application", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenApplicationDoesNotExist() {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    CandidateRepository candidateRepository = mock(CandidateRepository.class);
    JobRepository jobRepository = mock(JobRepository.class);
    CompanyRepository companyRepository = mock(CompanyRepository.class);
    UserService userService = mock(UserService.class);

    ApplicationService applicationService =
        new ApplicationService(
            applicationRepository,
            candidateRepository,
            jobRepository,
            companyRepository,
            userService);

    when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> applicationService.getApplicationById(999L));
  }

  @Test
  void shouldUpdateApplicationStatus() {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    CandidateRepository candidateRepository = mock(CandidateRepository.class);
    JobRepository jobRepository = mock(JobRepository.class);
    CompanyRepository companyRepository = mock(CompanyRepository.class);
    UserService userService = mock(UserService.class);

    ApplicationService applicationService =
        new ApplicationService(
            applicationRepository,
            candidateRepository,
            jobRepository,
            companyRepository,
            userService);

    Application existingApplication = new Application();

    existingApplication.setId(1L);
    existingApplication.setCandidateId(1L);
    existingApplication.setJobId(1L);
    existingApplication.setStatus("APPLIED");

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(existingApplication));

    User currentUser = new User();
    currentUser.setId(10L);
    currentUser.setEmail("candidate@example.com");
    currentUser.setRole("CANDIDATE");

    when(userService.getCurrentUser()).thenReturn(currentUser);

    Candidate candidate = new Candidate();
    candidate.setId(1L);
    candidate.setUserId(10L);

    when(candidateRepository.findByUserId(10L)).thenReturn(Optional.of(candidate));

    when(applicationRepository.save(any(Application.class))).thenReturn(existingApplication);

    UpdateApplicationRequest request = new UpdateApplicationRequest();

    request.setStatus("INTERVIEW");

    Application result = applicationService.updateApplication(1L, request);

    assertEquals("INTERVIEW", result.getStatus());

    verify(applicationRepository).findById(1L);

    verify(userService).getCurrentUser();

    verify(candidateRepository).findByUserId(10L);

    verify(applicationRepository).save(existingApplication);
  }
}
