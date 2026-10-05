package com.example.jobapplicationapi.job.controller;

import com.example.jobapplicationapi.job.dto.CreateJobRequest;
import com.example.jobapplicationapi.job.dto.UpdateJobRequest;
import com.example.jobapplicationapi.job.model.Job;
import com.example.jobapplicationapi.job.service.JobService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class JobController {

  private final JobService jobService;

  public JobController(JobService jobService) {
    this.jobService = jobService;
  }

  @GetMapping
  public List<Job> getAllJobs() {
    return jobService.getAllJobs();
  }

  @GetMapping("/{id}")
  public Job getJobById(@PathVariable Long id) {
    return jobService.getJobById(id);
  }

  @PostMapping
  public Job createJob(@Valid @RequestBody CreateJobRequest request) {

    Job savedJob = jobService.createJob(request);

    return savedJob;
  }

  @PutMapping("/{id}")
  public Job updateJob(@PathVariable Long id, @Valid @RequestBody UpdateJobRequest request) {

    Job updatedJob = jobService.updateJob(id, request);

    return updatedJob;
  }

  @DeleteMapping("/{id}")
  public void deleteJob(@PathVariable Long id) {
    jobService.deleteJob(id);
  }
}
