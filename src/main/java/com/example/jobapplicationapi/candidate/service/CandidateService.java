package com.example.jobapplicationapi.candidate.service;

import com.example.jobapplicationapi.candidate.dto.CreateCandidateRequest;
import com.example.jobapplicationapi.candidate.dto.UpdateCandidateRequest;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.exception.AuthorizationException;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CandidateService {
  private final CandidateRepository candidateRepository;
  private final UserService userService;

  public CandidateService(CandidateRepository candidateRepository, UserService userService) {

    this.candidateRepository = candidateRepository;
    this.userService = userService;
  }

  /*CrudRepository.findAll() returns:Iterable<Candidate>not:List<Candidate>So we're converting it to a List.*/
  public List<Candidate> getAllCandidates() {

    User currentUser = userService.getCurrentUser();

    Optional<Candidate> result = candidateRepository.findByUserId(currentUser.getId());

    List<Candidate> candidates = new ArrayList<>();

    if (result.isPresent()) {
      candidates.add(result.get());
    }

    return candidates;
  }

  public Candidate getCandidateById(Long id) {

    Optional<Candidate> result = candidateRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Candidate not found");
    }

    Candidate candidate = result.get();

    User currentUser = userService.getCurrentUser();

    if (!currentUser.getId().equals(candidate.getUserId())) {
      throw new AuthorizationException("You are not allowed to view this candidate");
    }

    return candidate;
  }

  public Candidate createCandidate(CreateCandidateRequest request) {

    User currentUser = userService.getCurrentUser();

    Candidate candidate = new Candidate();

    candidate.setUserId(currentUser.getId());
    candidate.setName(request.getName());
    candidate.setEmail(request.getEmail());
    candidate.setPhone(request.getPhone());
    candidate.setResumeUrl(request.getResumeUrl());

    Candidate savedCandidate = candidateRepository.save(candidate);

    return savedCandidate;
  }

  public void deleteCandidate(Long id) {

    Optional<Candidate> result = candidateRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Candidate not found");
    }

    Candidate existingCandidate = result.get();

    User currentUser = userService.getCurrentUser();

    if (!currentUser.getId().equals(existingCandidate.getUserId())) {
      throw new AuthorizationException("You are not allowed to delete this candidate");
    }

    candidateRepository.deleteById(id);
  }

  public Candidate updateCandidate(Long id, UpdateCandidateRequest request) {

    Optional<Candidate> result = candidateRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Candidate not found");
    }

    Candidate existingCandidate = result.get();

    User currentUser = userService.getCurrentUser();

    if (!currentUser.getId().equals(existingCandidate.getUserId())) {
      throw new AuthorizationException("You are not allowed to update this candidate");
    }

    existingCandidate.setName(request.getName());
    existingCandidate.setEmail(request.getEmail());
    existingCandidate.setPhone(request.getPhone());
    existingCandidate.setResumeUrl(request.getResumeUrl());

    Candidate savedCandidate = candidateRepository.save(existingCandidate);

    return savedCandidate;
  }
}
