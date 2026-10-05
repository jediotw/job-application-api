package com.example.jobapplicationapi.application.service;

import com.example.jobapplicationapi.application.dto.CreateApplicationRequest;
import com.example.jobapplicationapi.application.dto.UpdateApplicationRequest;
import com.example.jobapplicationapi.application.model.Application;
import com.example.jobapplicationapi.application.repository.ApplicationRepository;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.example.jobapplicationapi.job.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            CandidateRepository candidateRepository,
            JobRepository jobRepository) {

        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
    }

    public List<Application> getAllApplications() {

        List<Application> applications = new ArrayList<>();

        Iterable<Application> result =
                applicationRepository.findAll();

        for (Application application : result) {
            applications.add(application);
        }

        return applications;
    }

    public Application getApplicationById(Long id) {

        Optional<Application> result =
                applicationRepository.findById(id);

        if (!result.isPresent()) {
            throw new ResourceNotFoundException(
                    "Application not found"
            );
        }

        return result.get();
    }

    public Application createApplication(
            CreateApplicationRequest request) {

        boolean candidateExists =
                candidateRepository.existsById(
                        request.getCandidateId()
                );

        if (!candidateExists) {
            throw new ResourceNotFoundException(
                    "Candidate not found"
            );
        }

        boolean jobExists =
                jobRepository.existsById(
                        request.getJobId()
                );

        if (!jobExists) {
            throw new ResourceNotFoundException(
                    "Job not found"
            );
        }

        Application application = new Application();

        application.setCandidateId(
                request.getCandidateId()
        );

        application.setJobId(
                request.getJobId()
        );

        application.setStatus(
                request.getStatus()
        );

        application.setAppliedAt(
                LocalDateTime.now()
        );

        Application savedApplication =
                applicationRepository.save(application);

        return savedApplication;
    }

    public Application updateApplication(
            Long id,
            UpdateApplicationRequest request) {

        Optional<Application> result =
                applicationRepository.findById(id);

        if (!result.isPresent()) {
            throw new ResourceNotFoundException(
                    "Application not found"
            );
        }

        Application existingApplication = result.get();

        existingApplication.setStatus(
                request.getStatus()
        );

        Application savedApplication =
                applicationRepository.save(
                        existingApplication
                );

        return savedApplication;
    }

    public void deleteApplication(Long id) {

        boolean exists =
                applicationRepository.existsById(id);

        if (!exists) {
            throw new ResourceNotFoundException(
                    "Application not found"
            );
        }

        applicationRepository.deleteById(id);
    }
}