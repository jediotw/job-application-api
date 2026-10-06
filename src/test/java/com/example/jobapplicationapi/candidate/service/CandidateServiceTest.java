package com.example.jobapplicationapi.candidate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.jobapplicationapi.candidate.dto.CreateCandidateRequest;
import com.example.jobapplicationapi.candidate.dto.UpdateCandidateRequest;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.exception.AuthorizationException;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.service.UserService;
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

    // UserService is also a dependency of CandidateService.
    // We don't need it for this particular test,
    // but CandidateService requires it in its constructor.
    UserService userService = Mockito.mock(UserService.class);

    // Create the real CandidateService
    // and give it the fake dependencies.
    CandidateService candidateService = new CandidateService(candidateRepository, userService);

    // Create the candidate that we want
    // the fake repository to return.
    // The candidate belongs to the authenticated user.
    Candidate candidate = new Candidate();

    candidate.setId(1L);
    candidate.setUserId(10L);
    candidate.setName("Rahul Sharma");
    candidate.setEmail("rahul.sharma@example.com");

    // The authenticated user is also user 10.
    User currentUser = new User();

    currentUser.setId(10L);
    currentUser.setEmail("rahul.sharma@example.com");
    currentUser.setRole("CANDIDATE");

    Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);

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
  void shouldThrowExceptionWhenReadingCandidateOwnedByAnotherUser() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    CandidateService candidateService = new CandidateService(candidateRepository, userService);

    // Candidate belongs to user 20.
    Candidate existingCandidate = new Candidate();

    existingCandidate.setId(1L);
    existingCandidate.setUserId(20L);
    existingCandidate.setName("Rahul Sharma");
    existingCandidate.setEmail("rahul@example.com");

    Mockito.when(candidateRepository.findById(1L)).thenReturn(Optional.of(existingCandidate));

    // But the authenticated user is user 10.
    User currentUser = new User();

    currentUser.setId(10L);
    currentUser.setEmail("amit@example.com");
    currentUser.setRole("CANDIDATE");

    Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);

    // Act + Assert

    AuthorizationException exception =
        assertThrows(AuthorizationException.class, () -> candidateService.getCandidateById(1L));

    assertEquals("You are not allowed to view this candidate", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenCandidateDoesNotExist() {

    // Create fake dependencies.
    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    // Create real service with fake dependencies.
    CandidateService candidateService = new CandidateService(candidateRepository, userService);

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
  void shouldReturnOnlyCurrentUsersCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    CandidateService candidateService = new CandidateService(candidateRepository, userService);

    // The authenticated user is user 10.
    User currentUser = new User();

    currentUser.setId(10L);
    currentUser.setEmail("rahul@example.com");
    currentUser.setRole("CANDIDATE");

    Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);

    // Only the candidate profile that belongs to user 10 exists.
    Candidate candidate = new Candidate();

    candidate.setId(1L);
    candidate.setUserId(10L);
    candidate.setName("Rahul Sharma");
    candidate.setEmail("rahul.sharma@example.com");

    Mockito.when(candidateRepository.findByUserId(10L)).thenReturn(Optional.of(candidate));

    // Act

    List<Candidate> result = candidateService.getAllCandidates();

    // Assert

    assertEquals(1, result.size());

    assertEquals("Rahul Sharma", result.get(0).getName());

    // The service must scope the lookup to the authenticated user.
    Mockito.verify(candidateRepository).findByUserId(10L);

    Mockito.verify(candidateRepository, Mockito.never()).findAll();
  }

  @Test
  void shouldCreateCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    CandidateService candidateService = new CandidateService(candidateRepository, userService);

    // Create the authenticated user.
    User currentUser = new User();

    currentUser.setId(10L);
    currentUser.setEmail("amit@example.com");
    currentUser.setRole("CANDIDATE");

    // Tell UserService:
    // "The currently authenticated user is user ID 10."
    Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);

    CreateCandidateRequest request = new CreateCandidateRequest();

    request.setName("Amit Kumar");
    request.setEmail("amit.kumar@example.com");
    request.setPhone("9876543210");
    request.setResumeUrl("https://example.com/resumes/amit.pdf");

    Candidate savedCandidate = new Candidate();

    savedCandidate.setId(1L);
    savedCandidate.setUserId(10L);
    savedCandidate.setName("Amit Kumar");
    savedCandidate.setEmail("amit.kumar@example.com");
    savedCandidate.setPhone("9876543210");
    savedCandidate.setResumeUrl("https://example.com/resumes/amit.pdf");

    Mockito.when(candidateRepository.save(Mockito.any(Candidate.class))).thenReturn(savedCandidate);

    // Act

    Candidate result = candidateService.createCandidate(request);

    // Assert

    assertEquals(1L, result.getId());
    assertEquals(10L, result.getUserId());
    assertEquals("Amit Kumar", result.getName());
    assertEquals("amit.kumar@example.com", result.getEmail());

    // Verify that the service asked for the current user.
    Mockito.verify(userService).getCurrentUser();

    // Verify that the candidate was saved.
    Mockito.verify(candidateRepository).save(Mockito.any(Candidate.class));
  }

  @Test
  void shouldUpdateCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    CandidateService candidateService = new CandidateService(candidateRepository, userService);

    // Candidate belongs to user 10.
    Candidate existingCandidate = new Candidate();

    existingCandidate.setId(1L);
    existingCandidate.setUserId(10L);
    existingCandidate.setName("Rahul Sharma");
    existingCandidate.setEmail("rahul@example.com");
    existingCandidate.setPhone("9876543210");
    existingCandidate.setResumeUrl("https://example.com/old-resume.pdf");

    Mockito.when(candidateRepository.findById(1L)).thenReturn(Optional.of(existingCandidate));

    // Authenticated user is also user 10.
    User currentUser = new User();

    currentUser.setId(10L);
    currentUser.setEmail("rahul@example.com");
    currentUser.setRole("CANDIDATE");

    Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);

    UpdateCandidateRequest request = new UpdateCandidateRequest();

    request.setName("Rahul Kumar");
    request.setEmail("rahul.kumar@example.com");
    request.setPhone("9999999999");
    request.setResumeUrl("https://example.com/new-resume.pdf");

    Candidate savedCandidate = new Candidate();

    savedCandidate.setId(1L);
    savedCandidate.setUserId(10L);
    savedCandidate.setName("Rahul Kumar");
    savedCandidate.setEmail("rahul.kumar@example.com");
    savedCandidate.setPhone("9999999999");
    savedCandidate.setResumeUrl("https://example.com/new-resume.pdf");

    Mockito.when(candidateRepository.save(Mockito.any(Candidate.class))).thenReturn(savedCandidate);

    // Act

    Candidate result = candidateService.updateCandidate(1L, request);

    // Assert

    assertEquals(1L, result.getId());
    assertEquals(10L, result.getUserId());
    assertEquals("Rahul Kumar", result.getName());
    assertEquals("rahul.kumar@example.com", result.getEmail());
    assertEquals("9999999999", result.getPhone());
    assertEquals("https://example.com/new-resume.pdf", result.getResumeUrl());

    // Verify repository interaction.
    Mockito.verify(candidateRepository).findById(1L);

    // Verify that the service checked the current user.
    Mockito.verify(userService).getCurrentUser();

    // Verify save was called.
    Mockito.verify(candidateRepository).save(Mockito.any(Candidate.class));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingCandidateOwnedByAnotherUser() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    CandidateService candidateService = new CandidateService(candidateRepository, userService);

    // Candidate belongs to user 20.
    Candidate existingCandidate = new Candidate();

    existingCandidate.setId(1L);
    existingCandidate.setUserId(20L);
    existingCandidate.setName("Rahul Sharma");
    existingCandidate.setEmail("rahul@example.com");

    Mockito.when(candidateRepository.findById(1L)).thenReturn(Optional.of(existingCandidate));

    // But the authenticated user is user 10.
    User currentUser = new User();

    currentUser.setId(10L);
    currentUser.setEmail("amit@example.com");
    currentUser.setRole("CANDIDATE");

    Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);

    UpdateCandidateRequest request = new UpdateCandidateRequest();

    request.setName("Hacker");
    request.setEmail("hacker@example.com");
    request.setPhone("9999999999");
    request.setResumeUrl("https://example.com/hacker.pdf");

    // Act + Assert

    AuthorizationException exception =
        assertThrows(
            AuthorizationException.class, () -> candidateService.updateCandidate(1L, request));

    // Verify the exception message.
    assertEquals("You are not allowed to update this candidate", exception.getMessage());

    // Candidate must NOT be modified/saved.
    Mockito.verify(candidateRepository, Mockito.never()).save(Mockito.any(Candidate.class));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonExistingCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    CandidateService candidateService = new CandidateService(candidateRepository, userService);

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

    // Assert the exception message.
    assertEquals("Candidate not found", exception.getMessage());

    // Verify that save() was never called.
    Mockito.verify(candidateRepository, Mockito.never()).save(Mockito.any(Candidate.class));
  }

  @Test
  void shouldDeleteCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    CandidateService candidateService = new CandidateService(candidateRepository, userService);

    // Candidate belongs to user 10.
    Candidate existingCandidate = new Candidate();

    existingCandidate.setId(1L);
    existingCandidate.setUserId(10L);
    existingCandidate.setName("Rahul Sharma");

    Mockito.when(candidateRepository.findById(1L)).thenReturn(Optional.of(existingCandidate));

    // Authenticated user is user 10.
    User currentUser = new User();

    currentUser.setId(10L);
    currentUser.setEmail("rahul@example.com");
    currentUser.setRole("CANDIDATE");

    Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);

    // Act

    candidateService.deleteCandidate(1L);

    // Assert

    Mockito.verify(candidateRepository).findById(1L);

    Mockito.verify(userService).getCurrentUser();

    Mockito.verify(candidateRepository).deleteById(1L);
  }

  @Test
  void shouldThrowExceptionWhenDeletingCandidateOwnedByAnotherUser() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    CandidateService candidateService = new CandidateService(candidateRepository, userService);

    // Candidate belongs to user 20.
    Candidate existingCandidate = new Candidate();

    existingCandidate.setId(1L);
    existingCandidate.setUserId(20L);
    existingCandidate.setName("Rahul Sharma");

    Mockito.when(candidateRepository.findById(1L)).thenReturn(Optional.of(existingCandidate));

    // Authenticated user is user 10.
    User currentUser = new User();

    currentUser.setId(10L);
    currentUser.setEmail("amit@example.com");
    currentUser.setRole("CANDIDATE");

    Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);

    // Act + Assert

    AuthorizationException exception =
        assertThrows(AuthorizationException.class, () -> candidateService.deleteCandidate(1L));

    // Verify the exception message.
    assertEquals("You are not allowed to delete this candidate", exception.getMessage());

    // Candidate must NOT be deleted.
    Mockito.verify(candidateRepository, Mockito.never()).deleteById(1L);
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistingCandidate() {

    // Arrange

    CandidateRepository candidateRepository = Mockito.mock(CandidateRepository.class);
    UserService userService = Mockito.mock(UserService.class);

    CandidateService candidateService = new CandidateService(candidateRepository, userService);

    Mockito.when(candidateRepository.findById(999L)).thenReturn(Optional.empty());

    // Act + Assert

    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> candidateService.deleteCandidate(999L));

    assertEquals("Candidate not found", exception.getMessage());

    Mockito.verify(candidateRepository).findById(999L);

    Mockito.verify(candidateRepository, Mockito.never()).deleteById(999L);

    // Since the candidate doesn't exist,
    // ownership checking should never happen.
    Mockito.verify(userService, Mockito.never()).getCurrentUser();
  }
}
