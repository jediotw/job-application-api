package com.example.jobapplicationapi.application;

import com.example.jobapplicationapi.application.dto.CreateApplicationRequest;
import com.example.jobapplicationapi.application.model.Application;
import com.example.jobapplicationapi.application.repository.ApplicationRepository;
import com.example.jobapplicationapi.application.service.ApplicationService;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.example.jobapplicationapi.job.repository.JobRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApplicationServiceTest {

    @Test
    void shouldCreateApplication() {

        ApplicationRepository applicationRepository =
                mock(ApplicationRepository.class);

        CandidateRepository candidateRepository =
                mock(CandidateRepository.class);

        JobRepository jobRepository =
                mock(JobRepository.class);

        ApplicationService applicationService =
                new ApplicationService(
                        applicationRepository,
                        candidateRepository,
                        jobRepository
                );

        CreateApplicationRequest request =
                new CreateApplicationRequest();

        request.setCandidateId(1L);
        request.setJobId(1L);
        request.setStatus("APPLIED");

        when(candidateRepository.existsById(1L))
                .thenReturn(true);

        when(jobRepository.existsById(1L))
                .thenReturn(true);

        Application savedApplication =
                new Application();

        savedApplication.setId(1L);
        savedApplication.setCandidateId(1L);
        savedApplication.setJobId(1L);
        savedApplication.setStatus("APPLIED");

        when(applicationRepository.save(any(Application.class)))
                .thenReturn(savedApplication);

        Application result =
                applicationService.createApplication(request);

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getCandidateId());
        assertEquals(1L, result.getJobId());
        assertEquals("APPLIED", result.getStatus());

        verify(applicationRepository)
                .save(any(Application.class));
    }
    @Test
    void shouldThrowExceptionWhenCandidateDoesNotExist() {

        ApplicationRepository applicationRepository =
                mock(ApplicationRepository.class);

        CandidateRepository candidateRepository =
                mock(CandidateRepository.class);

        JobRepository jobRepository =
                mock(JobRepository.class);

        ApplicationService applicationService =
                new ApplicationService(
                        applicationRepository,
                        candidateRepository,
                        jobRepository
                );

        CreateApplicationRequest request =
                new CreateApplicationRequest();

        request.setCandidateId(999L);
        request.setJobId(1L);
        request.setStatus("APPLIED");

        when(candidateRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> applicationService.createApplication(request)
        );

        verify(applicationRepository, never())
                .save(any(Application.class));
    }
    @Test
    void shouldThrowExceptionWhenJobDoesNotExist() {

        ApplicationRepository applicationRepository =
                mock(ApplicationRepository.class);

        CandidateRepository candidateRepository =
                mock(CandidateRepository.class);

        JobRepository jobRepository =
                mock(JobRepository.class);

        ApplicationService applicationService =
                new ApplicationService(
                        applicationRepository,
                        candidateRepository,
                        jobRepository
                );

        CreateApplicationRequest request =
                new CreateApplicationRequest();

        request.setCandidateId(1L);
        request.setJobId(999L);
        request.setStatus("APPLIED");

        when(candidateRepository.existsById(1L))
                .thenReturn(true);

        when(jobRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> applicationService.createApplication(request)
        );

        verify(applicationRepository, never())
                .save(any(Application.class));
    }
    @Test
    void shouldGetApplicationById() {

        ApplicationRepository applicationRepository =
                mock(ApplicationRepository.class);

        CandidateRepository candidateRepository =
                mock(CandidateRepository.class);

        JobRepository jobRepository =
                mock(JobRepository.class);

        ApplicationService applicationService =
                new ApplicationService(
                        applicationRepository,
                        candidateRepository,
                        jobRepository
                );

        Application application =
                new Application();

        application.setId(1L);
        application.setCandidateId(1L);
        application.setJobId(1L);
        application.setStatus("APPLIED");

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        Application result =
                applicationService.getApplicationById(1L);

        assertEquals(1L, result.getId());
        assertEquals("APPLIED", result.getStatus());
    }
    @Test
    void shouldThrowExceptionWhenApplicationDoesNotExist() {

        ApplicationRepository applicationRepository =
                mock(ApplicationRepository.class);

        CandidateRepository candidateRepository =
                mock(CandidateRepository.class);

        JobRepository jobRepository =
                mock(JobRepository.class);

        ApplicationService applicationService =
                new ApplicationService(
                        applicationRepository,
                        candidateRepository,
                        jobRepository
                );

        when(applicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> applicationService.getApplicationById(999L)
        );
    }
    @Test
    void shouldUpdateApplicationStatus() {

        ApplicationRepository applicationRepository =
                mock(ApplicationRepository.class);

        CandidateRepository candidateRepository =
                mock(CandidateRepository.class);

        JobRepository jobRepository =
                mock(JobRepository.class);

        ApplicationService applicationService =
                new ApplicationService(
                        applicationRepository,
                        candidateRepository,
                        jobRepository
                );

        Application existingApplication =
                new Application();

        existingApplication.setId(1L);
        existingApplication.setCandidateId(1L);
        existingApplication.setJobId(1L);
        existingApplication.setStatus("APPLIED");

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(existingApplication));

        when(applicationRepository.save(any(Application.class)))
                .thenReturn(existingApplication);

        com.example.jobapplicationapi.application.dto.UpdateApplicationRequest request =
                new com.example.jobapplicationapi.application.dto.UpdateApplicationRequest();

        request.setStatus("INTERVIEW");

        Application result =
                applicationService.updateApplication(
                        1L,
                        request
                );

        assertEquals("INTERVIEW", result.getStatus());

        verify(applicationRepository)
                .save(existingApplication);
    }
}