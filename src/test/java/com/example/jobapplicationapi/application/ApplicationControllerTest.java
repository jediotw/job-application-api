package com.example.jobapplicationapi.application;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jobapplicationapi.application.controller.ApplicationController;
import com.example.jobapplicationapi.application.model.Application;
import com.example.jobapplicationapi.application.service.ApplicationService;
import com.example.jobapplicationapi.config.JwtService;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.example.jobapplicationapi.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ApplicationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ApplicationService applicationService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserRepository userRepository;

  @Test
  void shouldGetAllApplications() throws Exception {

    Application application1 = new Application();
    application1.setId(1L);
    application1.setCandidateId(1L);
    application1.setJobId(1L);
    application1.setStatus("APPLIED");

    Application application2 = new Application();
    application2.setId(2L);
    application2.setCandidateId(2L);
    application2.setJobId(2L);
    application2.setStatus("INTERVIEW");

    List<Application> applications = new ArrayList<>();

    applications.add(application1);
    applications.add(application2);

    when(applicationService.getAllApplications()).thenReturn(applications);

    mockMvc
        .perform(get("/applications"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].candidateId").value(1))
        .andExpect(jsonPath("$[0].jobId").value(1))
        .andExpect(jsonPath("$[0].status").value("APPLIED"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].status").value("INTERVIEW"));
  }

  @Test
  void shouldGetApplicationById() throws Exception {

    Application application = new Application();

    application.setId(1L);
    application.setCandidateId(1L);
    application.setJobId(1L);
    application.setStatus("APPLIED");

    when(applicationService.getApplicationById(1L)).thenReturn(application);

    mockMvc
        .perform(get("/applications/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.candidateId").value(1))
        .andExpect(jsonPath("$.jobId").value(1))
        .andExpect(jsonPath("$.status").value("APPLIED"));
  }

  @Test
  void shouldReturn404WhenApplicationDoesNotExist() throws Exception {

    when(applicationService.getApplicationById(999999L))
        .thenThrow(new ResourceNotFoundException("Application not found"));

    mockMvc
        .perform(get("/applications/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Application not found"));
  }

  @Test
  void shouldCreateApplication() throws Exception {

    Application application = new Application();

    application.setId(1L);
    application.setCandidateId(1L);
    application.setJobId(1L);
    application.setStatus("APPLIED");

    when(applicationService.createApplication(
            org.mockito.ArgumentMatchers.any(
                com.example.jobapplicationapi.application.dto.CreateApplicationRequest.class)))
        .thenReturn(application);

    String requestJson =
        """
            {
                "candidateId": 1,
                "jobId": 1,
                "status": "APPLIED"
            }
            """;

    mockMvc
        .perform(post("/applications").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.candidateId").value(1))
        .andExpect(jsonPath("$.jobId").value(1))
        .andExpect(jsonPath("$.status").value("APPLIED"));
  }

  @Test
  void shouldReturn400WhenCreatingInvalidApplication() throws Exception {

    String requestJson =
        """
            {
                "candidateId": null,
                "jobId": null,
                "status": ""
            }
            """;

    mockMvc
        .perform(post("/applications").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Validation failed"));
  }

  @Test
  void shouldUpdateApplication() throws Exception {

    Application application = new Application();

    application.setId(1L);
    application.setCandidateId(1L);
    application.setJobId(1L);
    application.setStatus("INTERVIEW");

    when(applicationService.updateApplication(
            org.mockito.ArgumentMatchers.eq(1L),
            org.mockito.ArgumentMatchers.any(
                com.example.jobapplicationapi.application.dto.UpdateApplicationRequest.class)))
        .thenReturn(application);

    String requestJson =
        """
            {
                "status": "INTERVIEW"
            }
            """;

    mockMvc
        .perform(
            put("/applications/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.candidateId").value(1))
        .andExpect(jsonPath("$.jobId").value(1))
        .andExpect(jsonPath("$.status").value("INTERVIEW"));
  }

  @Test
  void shouldReturn404WhenUpdatingNonExistingApplication() throws Exception {

    when(applicationService.updateApplication(
            org.mockito.ArgumentMatchers.eq(999999L),
            org.mockito.ArgumentMatchers.any(
                com.example.jobapplicationapi.application.dto.UpdateApplicationRequest.class)))
        .thenThrow(new ResourceNotFoundException("Application not found"));

    String requestJson =
        """
            {
                "status": "INTERVIEW"
            }
            """;

    mockMvc
        .perform(
            put("/applications/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Application not found"));
  }

  @Test
  void shouldDeleteApplication() throws Exception {

    mockMvc.perform(delete("/applications/1")).andExpect(status().isOk());
  }

  @Test
  void shouldReturn404WhenDeletingNonExistingApplication() throws Exception {

    org.mockito.Mockito.doThrow(new ResourceNotFoundException("Application not found"))
        .when(applicationService)
        .deleteApplication(999999L);

    mockMvc
        .perform(delete("/applications/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Application not found"));
  }

  @Test
  void shouldReturn400WhenUpdatingInvalidApplication() throws Exception {

    String requestJson =
        """
            {
                "status": ""
            }
            """;

    mockMvc
        .perform(
            put("/applications/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Validation failed"));
  }
}
