package com.example.jobapplicationapi.candidate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.jobapplicationapi.candidate.dto.CreateCandidateRequest;
import com.example.jobapplicationapi.candidate.dto.UpdateCandidateRequest;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

// JUnit = testing framework
// Mockito = dependency mocking framework

public class CandidateServiceTest {

  @Test
  void shouldReturnCandidateWhenCandidateExists() {

    // Mockito creates a fake CandidateRepository.
    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);

    // Create the real CandidateService
    // and give it the fake repository.
    CandidateService candidateService = new CandidateService(candidateRepository);

    // Create the candidate that we want
    // the fake repository to return.
    Candidate candidate = new Candidate();

    candidate.setId(1L);
    candidate.setName("Rahul Sharma");
    candidate.setEmail("rahul.sharma@example.com");

    // WHEN the service asks the repository for ID 1,
    // THEN the fake repository returns this candidate.
    Mockito.when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));

    // Call the REAL service method.
    Candidate result = candidateService.getCandidateById(1L);

    // Verify the result.
    assertEquals(1L, result.getId());
    assertEquals("Rahul Sharma", result.getName());
    assertEquals("rahul.sharma@example.com", result.getEmail());
  }

  @Test
  void shouldThrowExceptionWhenCandidateDoesNotExist() {

    // Create fake repository.
    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);

    // Create real service with fake repository.
    CandidateService candidateService = new CandidateService(candidateRepository);

    // Tell the fake repository:
    // "If someone asks for candidate 999,
    // pretend that candidate does not exist."
    Mockito.when(candidateRepository.findById(999L)).thenReturn(Optional.empty());

    // Call the real service and EXPECT
    // ResourceNotFoundException to be thrown.
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class, () -> candidateService.getCandidateById(999L));

    // Verify the exception message.
    assertEquals("Candidate not found", exception.getMessage());
  }

  @Test
  void shouldReturnAllCandidates() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);

    CandidateService candidateService = new CandidateService(candidateRepository);

    Candidate candidate1 = new Candidate();
    candidate1.setId(1L);
    candidate1.setName("Rahul Sharma");
    candidate1.setEmail("rahul.sharma@example.com");

    Candidate candidate2 = new Candidate();
    candidate2.setId(2L);
    candidate2.setName("Priya Singh");
    candidate2.setEmail("priya.singh@example.com");

    List<Candidate> candidates = new ArrayList<>();

    candidates.add(candidate1);
    candidates.add(candidate2);

    Mockito.when(candidateRepository.findAll()).thenReturn(candidates);

    // Act

    List<Candidate> result = candidateService.getAllCandidates();

    // Assert

    assertEquals(2, result.size());

    assertEquals("Rahul Sharma", result.get(0).getName());

    assertEquals("Priya Singh", result.get(1).getName());
  }

  @Test
  void shouldCreateCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);

    CandidateService candidateService = new CandidateService(candidateRepository);

    CreateCandidateRequest request = new CreateCandidateRequest();

    request.setName("Amit Kumar");
    request.setEmail("amit.kumar@example.com");
    request.setPhone("9876543210");
    request.setResumeUrl("https://example.com/resumes/amit.pdf");

    Candidate savedCandidate = new Candidate();

    savedCandidate.setId(1L);
    savedCandidate.setName("Amit Kumar");
    savedCandidate.setEmail("amit.kumar@example.com");
    savedCandidate.setPhone("9876543210");
    savedCandidate.setResumeUrl("https://example.com/resumes/amit.pdf");

    Mockito.when(candidateRepository.save(Mockito.any(Candidate.class))).thenReturn(savedCandidate);

    // Act

    Candidate result = candidateService.createCandidate(request);

    // Assert

    assertEquals(1L, result.getId());
    assertEquals("Amit Kumar", result.getName());
    assertEquals("amit.kumar@example.com", result.getEmail());

    Mockito.verify(candidateRepository).save(Mockito.any(Candidate.class));
  }

  @Test
  void shouldUpdateCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);

    CandidateService candidateService = new CandidateService(candidateRepository);

    Candidate existingCandidate = new Candidate();

    existingCandidate.setId(1L);
    existingCandidate.setName("Rahul Sharma");
    existingCandidate.setEmail("rahul@example.com");
    existingCandidate.setPhone("9876543210");
    existingCandidate.setResumeUrl("https://example.com/old-resume.pdf");

    Mockito.when(candidateRepository.findById(1L)).thenReturn(Optional.of(existingCandidate));

    UpdateCandidateRequest request = new UpdateCandidateRequest();

    request.setName("Rahul Kumar");
    request.setEmail("rahul.kumar@example.com");
    request.setPhone("9999999999");
    request.setResumeUrl("https://example.com/new-resume.pdf");

    Candidate savedCandidate = new Candidate();

    savedCandidate.setId(1L);
    savedCandidate.setName("Rahul Kumar");
    savedCandidate.setEmail("rahul.kumar@example.com");
    savedCandidate.setPhone("9999999999");
    savedCandidate.setResumeUrl("https://example.com/new-resume.pdf");

    Mockito.when(candidateRepository.save(Mockito.any(Candidate.class))).thenReturn(savedCandidate);

    // Act

    Candidate result = candidateService.updateCandidate(1L, request);

    // Assert

    assertEquals(1L, result.getId());

    assertEquals("Rahul Kumar", result.getName());

    assertEquals("rahul.kumar@example.com", result.getEmail());

    assertEquals("9999999999", result.getPhone());

    assertEquals("https://example.com/new-resume.pdf", result.getResumeUrl());

    Mockito.verify(candidateRepository).findById(1L);

    Mockito.verify(candidateRepository).save(Mockito.any(Candidate.class));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonExistingCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);

    CandidateService candidateService = new CandidateService(candidateRepository);

    Mockito.when(candidateRepository.findById(999L)).thenReturn(Optional.empty());

    UpdateCandidateRequest request = new UpdateCandidateRequest();

    request.setName("Rahul Kumar");
    request.setEmail("rahul.kumar@example.com");
    request.setPhone("9999999999");
    request.setResumeUrl("https://example.com/new-resume.pdf");

    // Act + Assert

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class, () -> candidateService.updateCandidate(999L, request));

    // Assert the exception message

    assertEquals("Candidate not found", exception.getMessage());

    // Verify that save() was never called

    Mockito.verify(candidateRepository, Mockito.never()).save(Mockito.any(Candidate.class));
  }

  @Test
  void shouldDeleteCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);

    CandidateService candidateService = new CandidateService(candidateRepository);

    Mockito.when(candidateRepository.existsById(1L)).thenReturn(true);

    // Act

    candidateService.deleteCandidate(1L);

    // Assert

    Mockito.verify(candidateRepository).existsById(1L);

    Mockito.verify(candidateRepository).deleteById(1L);
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistingCandidate() {

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);

    CandidateService candidateService = new CandidateService(candidateRepository);

    Mockito.when(candidateRepository.existsById(999L)).thenReturn(false);

    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> candidateService.deleteCandidate(999L));

    assertEquals("Candidate not found", exception.getMessage());

    Mockito.verify(candidateRepository).existsById(999L);

    Mockito.verify(candidateRepository, Mockito.never()).deleteById(999L);
  }
}
