package com.example.jobapplicationapi.application.service;

import com.example.jobapplicationapi.application.dto.CreateApplicationRequest;
import com.example.jobapplicationapi.application.dto.UpdateApplicationRequest;
import com.example.jobapplicationapi.application.model.Application;
import com.example.jobapplicationapi.application.repository.ApplicationRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final CandidateRepository candidateRepository;
  private final JobRepository jobRepository;
  private final CompanyRepository companyRepository;
  private final UserService userService;

  public ApplicationService(
      ApplicationRepository applicationRepository,
      CandidateRepository candidateRepository,
      JobRepository jobRepository,
      CompanyRepository companyRepository,
      UserService userService) {

    this.applicationRepository = applicationRepository;
    this.candidateRepository = candidateRepository;
    this.jobRepository = jobRepository;
    this.companyRepository = companyRepository;
    this.userService = userService;
  }

  public List<Application> getAllApplications() {

    User currentUser = userService.getCurrentUser();

    if (currentUser.getRole().equals("CANDIDATE")) {

      Optional<Candidate> candidateResult = candidateRepository.findByUserId(currentUser.getId());

      if (!candidateResult.isPresent()) {
        throw new ResourceNotFoundException("Candidate not found");
      }

      Candidate candidate = candidateResult.get();

      return applicationRepository.findByCandidateId(candidate.getId());
    }

    if (currentUser.getRole().equals("RECRUITER")) {
      return applicationRepository.findByRecruiterId(currentUser.getId());
    }

    return new ArrayList<>();
  }

  public Application getApplicationById(Long id) {

    Optional<Application> result = applicationRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Application not found");
    }

    Application application = result.get();

    User currentUser = userService.getCurrentUser();

    if (currentUser.getRole().equals("CANDIDATE")) {

      Optional<Candidate> candidateResult = candidateRepository.findByUserId(currentUser.getId());

      if (!candidateResult.isPresent()) {
        throw new ResourceNotFoundException("Candidate not found");
      }

      Candidate candidate = candidateResult.get();

      if (!application.getCandidateId().equals(candidate.getId())) {
        throw new AuthorizationException("You are not allowed to view this application");
      }

    } else if (currentUser.getRole().equals("RECRUITER")) {

      Optional<Job> jobResult = jobRepository.findById(application.getJobId());

      if (!jobResult.isPresent()) {
        throw new ResourceNotFoundException("Job not found");
      }

      Job job = jobResult.get();

      Optional<Company> companyResult = companyRepository.findById(job.getCompanyId());

      if (!companyResult.isPresent()) {
        throw new ResourceNotFoundException("Company not found");
      }

      Company company = companyResult.get();

      if (!currentUser.getId().equals(company.getRecruiterId())) {
        throw new AuthorizationException("You are not allowed to view this application");
      }
    }

    return application;
  }

  public Application createApplication(CreateApplicationRequest request) {

    User currentUser = userService.getCurrentUser();

    Optional<Candidate> candidateResult = candidateRepository.findByUserId(currentUser.getId());

    if (!candidateResult.isPresent()) {
      throw new ResourceNotFoundException("Candidate not found");
    }

    Optional<Job> jobResult = jobRepository.findById(request.getJobId());

    if (!jobResult.isPresent()) {
      throw new ResourceNotFoundException("Job not found");
    }

    Candidate candidate = candidateResult.get();

    Application application = new Application();

    application.setCandidateId(candidate.getId());
    application.setJobId(request.getJobId());
    application.setStatus(request.getStatus());
    application.setAppliedAt(LocalDateTime.now());

    Application savedApplication = applicationRepository.save(application);

    return savedApplication;
  }

  public Application updateApplication(Long id, UpdateApplicationRequest request) {

    Optional<Application> result = applicationRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Application not found");
    }

    Application existingApplication = result.get();

    User currentUser = userService.getCurrentUser();

    if (currentUser.getRole().equals("CANDIDATE")) {

      Optional<Candidate> candidateResult = candidateRepository.findByUserId(currentUser.getId());

      if (!candidateResult.isPresent()) {
        throw new ResourceNotFoundException("Candidate not found");
      }

      Candidate candidate = candidateResult.get();

      if (!existingApplication.getCandidateId().equals(candidate.getId())) {
        throw new AuthorizationException("You are not allowed to update this application");
      }

    } else if (currentUser.getRole().equals("RECRUITER")) {

      Optional<Job> jobResult = jobRepository.findById(existingApplication.getJobId());

      if (!jobResult.isPresent()) {
        throw new ResourceNotFoundException("Job not found");
      }

      Job job = jobResult.get();

      Optional<Company> companyResult = companyRepository.findById(job.getCompanyId());

      if (!companyResult.isPresent()) {
        throw new ResourceNotFoundException("Company not found");
      }

      Company company = companyResult.get();

      if (!currentUser.getId().equals(company.getRecruiterId())) {
        throw new AuthorizationException("You are not allowed to update this application");
      }
    }

    existingApplication.setStatus(request.getStatus());

    Application savedApplication = applicationRepository.save(existingApplication);

    return savedApplication;
  }

  public void deleteApplication(Long id) {

    Optional<Application> result = applicationRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Application not found");
    }

    Application existingApplication = result.get();

    User currentUser = userService.getCurrentUser();

    if (currentUser.getRole().equals("CANDIDATE")) {

      Optional<Candidate> candidateResult = candidateRepository.findByUserId(currentUser.getId());

      if (!candidateResult.isPresent()) {
        throw new ResourceNotFoundException("Candidate not found");
      }

      Candidate candidate = candidateResult.get();

      if (!existingApplication.getCandidateId().equals(candidate.getId())) {
        throw new AuthorizationException("You are not allowed to delete this application");
      }

    } else if (currentUser.getRole().equals("RECRUITER")) {

      Optional<Job> jobResult = jobRepository.findById(existingApplication.getJobId());

      if (!jobResult.isPresent()) {
        throw new ResourceNotFoundException("Job not found");
      }

      Job job = jobResult.get();

      Optional<Company> companyResult = companyRepository.findById(job.getCompanyId());

      if (!companyResult.isPresent()) {
        throw new ResourceNotFoundException("Company not found");
      }

      Company company = companyResult.get();

      if (!currentUser.getId().equals(company.getRecruiterId())) {
        throw new AuthorizationException("You are not allowed to delete this application");
      }
    }

    applicationRepository.deleteById(id);
  }
}
