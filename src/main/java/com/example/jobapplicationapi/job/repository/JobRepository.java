package com.example.jobapplicationapi.job.repository;

import com.example.jobapplicationapi.job.model.Job;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends CrudRepository<Job, Long> {}
