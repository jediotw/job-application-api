package com.example.jobapplicationapi.candidate.service;

import com.example.jobapplicationapi.candidate.dto.CreateCandidateRequest;
import com.example.jobapplicationapi.candidate.dto.UpdateCandidateRequest;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CandidateService {
    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository){
        this.candidateRepository=candidateRepository;
    }

    /*CrudRepository.findAll() returns:Iterable<Candidate>not:List<Candidate>So we're converting it to a List.*/
    public List<Candidate> getAllCandidates() {

        List<Candidate> candidates = new ArrayList<>();

        Iterable<Candidate> result = candidateRepository.findAll();

        for (Candidate candidate : result) {
            candidates.add(candidate);
        }

        return candidates;
    }

    public Candidate getCandidateById(Long id) {
    //since find by id returns optional
        Optional<Candidate> result = candidateRepository.findById(id);

        if (result.isPresent()) {
            return result.get();
        }

        throw new RuntimeException("Candidate not found");
    }
    public Candidate createCandidate(CreateCandidateRequest request) {

        Candidate candidate = new Candidate();

        candidate.setName(request.getName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setResume_url(request.getResumeUrl());

        Candidate savedCandidate = candidateRepository.save(candidate);

        return savedCandidate;
    }



    public void deleteCandidate(Long id) {

        boolean exists = candidateRepository.existsById(id);

        if (!exists) {
            throw new RuntimeException("Candidate not found");
        }

        candidateRepository.deleteById(id);
    }
    public Candidate updateCandidate(
            Long id,
            UpdateCandidateRequest request) {

        Optional<Candidate> result = candidateRepository.findById(id);

        if (!result.isPresent()) {
            throw new RuntimeException("Candidate not found");
        }

        Candidate existingCandidate = result.get();

        existingCandidate.setName(request.getName());
        existingCandidate.setEmail(request.getEmail());
        existingCandidate.setPhone(request.getPhone());
        existingCandidate.setResume_url(request.getResumeUrl());

        Candidate savedCandidate = candidateRepository.save(existingCandidate);

        return savedCandidate;
    }

}
