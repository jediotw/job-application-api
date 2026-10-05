package com.example.jobapplicationapi.application.controller;

import com.example.jobapplicationapi.application.dto.CreateApplicationRequest;
import com.example.jobapplicationapi.application.dto.UpdateApplicationRequest;
import com.example.jobapplicationapi.application.model.Application;
import com.example.jobapplicationapi.application.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

  private final ApplicationService applicationService;

  public ApplicationController(ApplicationService applicationService) {

    this.applicationService = applicationService;
  }

  @GetMapping
  public List<Application> getAllApplications() {

    return applicationService.getAllApplications();
  }

  @GetMapping("/{id}")
  public Application getApplicationById(@PathVariable Long id) {

    return applicationService.getApplicationById(id);
  }

  @PostMapping
  public Application createApplication(@Valid @RequestBody CreateApplicationRequest request) {

    Application savedApplication = applicationService.createApplication(request);

    return savedApplication;
  }

  @PutMapping("/{id}")
  public Application updateApplication(
      @PathVariable Long id, @Valid @RequestBody UpdateApplicationRequest request) {

    Application updatedApplication = applicationService.updateApplication(id, request);

    return updatedApplication;
  }

  @DeleteMapping("/{id}")
  public void deleteApplication(@PathVariable Long id) {

    applicationService.deleteApplication(id);
  }
}
