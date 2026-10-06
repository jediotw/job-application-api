package com.example.jobapplicationapi.candidate.repository;

import com.example.jobapplicationapi.candidate.model.Candidate;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

// after extending the interface methods of crud repository we have all the crud boilerplate methods
// of database with us
// You don't need to redeclare it unless you specifically want to change the return type to a
// compatible subtype such as List<Candidate> and Spring Data JDBC supports that override.
public interface CandidateRepository extends CrudRepository<Candidate, Long> {
  Optional<Candidate> findByUserId(Long userId);
}
