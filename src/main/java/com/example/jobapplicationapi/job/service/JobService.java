package com.example.jobapplicationapi.job.service;

import com.example.jobapplicationapi.company.model.Company;
import com.example.jobapplicationapi.company.repository.CompanyRepository;
import com.example.jobapplicationapi.exception.AuthorizationException;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.example.jobapplicationapi.job.dto.CreateJobRequest;
import com.example.jobapplicationapi.job.dto.UpdateJobRequest;
import com.example.jobapplicationapi.job.model.Job;
import com.example.jobapplicationapi.job.repository.JobRepository;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class JobService {

  private final JobRepository jobRepository;
  private final CompanyRepository companyRepository;
  private final UserService userService;

  public JobService(
      JobRepository jobRepository, CompanyRepository companyRepository, UserService userService) {

    this.jobRepository = jobRepository;
    this.companyRepository = companyRepository;
    this.userService = userService;
  }

  public List<Job> getAllJobs() {

    List<Job> jobs = new ArrayList<>();

    Iterable<Job> result = jobRepository.findAll();

    for (Job job : result) {
      jobs.add(job);
    }

    return jobs;
  }

  public Job getJobById(Long id) {

    Optional<Job> result = jobRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Job not found");
    }

    return result.get();
  }

  public Job createJob(CreateJobRequest request) {

    Optional<Company> companyResult = companyRepository.findById(request.getCompanyId());

    if (!companyResult.isPresent()) {
      throw new ResourceNotFoundException("Company not found");
    }

    Company company = companyResult.get();

    User currentUser = userService.getCurrentUser();

    if (!currentUser.getId().equals(company.getRecruiterId())) {
      throw new AuthorizationException("You are not allowed to create a job for this company");
    }

    Job job = new Job();

    job.setCompanyId(request.getCompanyId());
    job.setTitle(request.getTitle());
    job.setDescription(request.getDescription());
    job.setLocation(request.getLocation());
    job.setEmploymentType(request.getEmploymentType());
    job.setSalaryMin(request.getSalaryMin());
    job.setSalaryMax(request.getSalaryMax());

    Job savedJob = jobRepository.save(job);

    return savedJob;
  }

  public Job updateJob(Long id, UpdateJobRequest request) {

    Optional<Job> result = jobRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Job not found");
    }

    Job existingJob = result.get();

    User currentUser = userService.getCurrentUser();

    Optional<Company> existingCompanyResult =
        companyRepository.findById(existingJob.getCompanyId());

    if (!existingCompanyResult.isPresent()) {
      throw new ResourceNotFoundException("Company not found");
    }

    if (!currentUser.getId().equals(existingCompanyResult.get().getRecruiterId())) {
      throw new AuthorizationException("You are not allowed to update this job");
    }

    Optional<Company> companyResult = companyRepository.findById(request.getCompanyId());

    if (!companyResult.isPresent()) {
      throw new ResourceNotFoundException("Company not found");
    }

    Company company = companyResult.get();

    if (!currentUser.getId().equals(company.getRecruiterId())) {
      throw new AuthorizationException("You are not allowed to update this job");
    }

    existingJob.setCompanyId(request.getCompanyId());
    existingJob.setTitle(request.getTitle());
    existingJob.setDescription(request.getDescription());
    existingJob.setLocation(request.getLocation());
    existingJob.setEmploymentType(request.getEmploymentType());
    existingJob.setSalaryMin(request.getSalaryMin());
    existingJob.setSalaryMax(request.getSalaryMax());

    Job savedJob = jobRepository.save(existingJob);

    return savedJob;
  }

  public void deleteJob(Long id) {

    Optional<Job> result = jobRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Job not found");
    }

    Job existingJob = result.get();

    Optional<Company> companyResult = companyRepository.findById(existingJob.getCompanyId());

    if (!companyResult.isPresent()) {
      throw new ResourceNotFoundException("Company not found");
    }

    Company company = companyResult.get();

    User currentUser = userService.getCurrentUser();

    if (!currentUser.getId().equals(company.getRecruiterId())) {
      throw new AuthorizationException("You are not allowed to delete this job");
    }

    jobRepository.deleteById(id);
  }
}
