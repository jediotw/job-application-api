package com.example.jobapplicationapi.candidate.controller;

import com.example.jobapplicationapi.candidate.dto.CreateCandidateRequest;
import com.example.jobapplicationapi.candidate.dto.UpdateCandidateRequest;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidates")
public class CandidateController {
    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService){
        this.candidateService=candidateService;
    }

    @GetMapping
    public List<Candidate> getAllCandidates(){
    return candidateService.getAllCandidates();
    }
    @GetMapping("/{id}")
    public Candidate getCandidateById(@PathVariable Long id) {
        return candidateService.getCandidateById(id);
    }

    @PostMapping
    public Candidate createCandidate(
             @Valid  @RequestBody CreateCandidateRequest request) {

        Candidate savedCandidate = candidateService.createCandidate(request);

        return savedCandidate;
    }

    @DeleteMapping("/{id}")
    public void deleteCandidate(@PathVariable Long id) {

        candidateService.deleteCandidate(id);
    }

    @PutMapping("/{id}")
    public Candidate updateCandidate(
            @PathVariable Long id,
            @RequestBody UpdateCandidateRequest request) {

        Candidate updatedCandidate =
                candidateService.updateCandidate(id, request);

        return updatedCandidate;
    }




}
