package com.example.jobapplicationapi.job.service;

import com.example.jobapplicationapi.company.repository.CompanyRepository;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.example.jobapplicationapi.job.dto.CreateJobRequest;
import com.example.jobapplicationapi.job.dto.UpdateJobRequest;
import com.example.jobapplicationapi.job.model.Job;
import com.example.jobapplicationapi.job.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobService(
            JobRepository jobRepository,
            CompanyRepository companyRepository) {

        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
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

        Optional<Job> result =
                jobRepository.findById(id);

        if (!result.isPresent()) {
            throw new ResourceNotFoundException("Job not found");
        }

        return result.get();
    }

    public Job createJob(CreateJobRequest request) {

        boolean companyExists =
                companyRepository.existsById(request.getCompanyId());

        if (!companyExists) {
            throw new ResourceNotFoundException("Company not found");
        }

        Job job = new Job();

        job.setCompanyId(request.getCompanyId());
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());

        Job savedJob =
                jobRepository.save(job);

        return savedJob;
    }

    public Job updateJob(
            Long id,
            UpdateJobRequest request) {

        Optional<Job> result =
                jobRepository.findById(id);

        if (!result.isPresent()) {
            throw new ResourceNotFoundException("Job not found");
        }

        boolean companyExists =
                companyRepository.existsById(request.getCompanyId());

        if (!companyExists) {
            throw new ResourceNotFoundException("Company not found");
        }

        Job existingJob = result.get();

        existingJob.setCompanyId(request.getCompanyId());
        existingJob.setTitle(request.getTitle());
        existingJob.setDescription(request.getDescription());
        existingJob.setLocation(request.getLocation());
        existingJob.setEmploymentType(request.getEmploymentType());
        existingJob.setSalaryMin(request.getSalaryMin());
        existingJob.setSalaryMax(request.getSalaryMax());

        Job savedJob =
                jobRepository.save(existingJob);

        return savedJob;
    }

    public void deleteJob(Long id) {

        boolean exists =
                jobRepository.existsById(id);

        if (!exists) {
            throw new ResourceNotFoundException("Job not found");
        }

        jobRepository.deleteById(id);
    }
}