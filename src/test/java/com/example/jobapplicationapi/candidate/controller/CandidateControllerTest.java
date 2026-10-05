package com.example.jobapplicationapi.candidate.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jobapplicationapi.candidate.dto.CreateCandidateRequest;
import com.example.jobapplicationapi.candidate.dto.UpdateCandidateRequest;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.service.CandidateService;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CandidateController.class)
public class CandidateControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CandidateService candidateService;

  @Autowired private ObjectMapper objectMapper;

  // ---------------------------------------------------------
  // GET /candidates
  // ---------------------------------------------------------

  @Test
  void shouldReturnAllCandidates() throws Exception {

    Candidate candidate1 = new Candidate();

    candidate1.setId(1L);
    candidate1.setName("Rahul Sharma");
    candidate1.setEmail("rahul@example.com");

    Candidate candidate2 = new Candidate();

    candidate2.setId(2L);
    candidate2.setName("Amit Kumar");
    candidate2.setEmail("amit@example.com");

    List<Candidate> candidates = new ArrayList<>();

    candidates.add(candidate1);
    candidates.add(candidate2);

    when(candidateService.getAllCandidates()).thenReturn(candidates);

    mockMvc
        .perform(get("/candidates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Rahul Sharma"))
        .andExpect(jsonPath("$[0].email").value("rahul@example.com"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].name").value("Amit Kumar"))
        .andExpect(jsonPath("$[1].email").value("amit@example.com"));

    verify(candidateService).getAllCandidates();
  }

  // ---------------------------------------------------------
  // GET /candidates/{id} - success
  // ---------------------------------------------------------

  @Test
  void shouldReturnCandidate() throws Exception {

    Candidate candidate = new Candidate();

    candidate.setId(1L);
    candidate.setName("Rahul Sharma");
    candidate.setEmail("rahul@example.com");
    candidate.setPhone("9876543210");
    candidate.setResumeUrl("https://example.com/rahul.pdf");

    when(candidateService.getCandidateById(1L)).thenReturn(candidate);

    mockMvc
        .perform(get("/candidates/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Rahul Sharma"))
        .andExpect(jsonPath("$.email").value("rahul@example.com"))
        .andExpect(jsonPath("$.phone").value("9876543210"))
        .andExpect(jsonPath("$.resumeUrl").value("https://example.com/rahul.pdf"));

    verify(candidateService).getCandidateById(1L);
  }

  // ---------------------------------------------------------
  // GET /candidates/{id} - not found
  // ---------------------------------------------------------

  @Test
  void shouldReturn404WhenCandidateDoesNotExist() throws Exception {

    when(candidateService.getCandidateById(999L))
        .thenThrow(new ResourceNotFoundException("Candidate not found"));

    mockMvc
        .perform(get("/candidates/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Candidate not found"));

    verify(candidateService).getCandidateById(999L);
  }

  // ---------------------------------------------------------
  // POST /candidates - success
  // ---------------------------------------------------------

  @Test
  void shouldCreateCandidate() throws Exception {

    CreateCandidateRequest request = new CreateCandidateRequest();

    request.setName("Rahul Sharma");
    request.setEmail("rahul@example.com");
    request.setPhone("9876543210");
    request.setResumeUrl("https://example.com/rahul.pdf");

    Candidate savedCandidate = new Candidate();

    savedCandidate.setId(1L);
    savedCandidate.setName("Rahul Sharma");
    savedCandidate.setEmail("rahul@example.com");
    savedCandidate.setPhone("9876543210");
    savedCandidate.setResumeUrl("https://example.com/rahul.pdf");

    when(candidateService.createCandidate(any(CreateCandidateRequest.class)))
        .thenReturn(savedCandidate);

    String requestJson = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(post("/candidates").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Rahul Sharma"))
        .andExpect(jsonPath("$.email").value("rahul@example.com"))
        .andExpect(jsonPath("$.phone").value("9876543210"))
        .andExpect(jsonPath("$.resumeUrl").value("https://example.com/rahul.pdf"));

    verify(candidateService).createCandidate(any(CreateCandidateRequest.class));
  }

  // ---------------------------------------------------------
  // POST /candidates - validation failure
  // ---------------------------------------------------------

  @Test
  void shouldReturn400WhenCreatingInvalidCandidate() throws Exception {

    CreateCandidateRequest request = new CreateCandidateRequest();

    request.setName("");
    request.setEmail("invalid-email");

    String requestJson = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(post("/candidates").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Validation failed"));

    verify(candidateService, never()).createCandidate(any(CreateCandidateRequest.class));
  }

  // ---------------------------------------------------------
  // PUT /candidates/{id} - success
  // ---------------------------------------------------------

  @Test
  void shouldUpdateCandidate() throws Exception {

    UpdateCandidateRequest request = new UpdateCandidateRequest();

    request.setName("Rahul Updated");
    request.setEmail("rahul.updated@example.com");
    request.setPhone("9999999999");
    request.setResumeUrl("https://example.com/updated.pdf");

    Candidate updatedCandidate = new Candidate();

    updatedCandidate.setId(1L);
    updatedCandidate.setName("Rahul Updated");
    updatedCandidate.setEmail("rahul.updated@example.com");
    updatedCandidate.setPhone("9999999999");
    updatedCandidate.setResumeUrl("https://example.com/updated.pdf");

    when(candidateService.updateCandidate(any(Long.class), any(UpdateCandidateRequest.class)))
        .thenReturn(updatedCandidate);

    String requestJson = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(put("/candidates/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Rahul Updated"))
        .andExpect(jsonPath("$.email").value("rahul.updated@example.com"))
        .andExpect(jsonPath("$.phone").value("9999999999"))
        .andExpect(jsonPath("$.resumeUrl").value("https://example.com/updated.pdf"));

    verify(candidateService).updateCandidate(any(Long.class), any(UpdateCandidateRequest.class));
  }

  // ---------------------------------------------------------
  // PUT /candidates/{id} - not found
  // ---------------------------------------------------------

  @Test
  void shouldReturn404WhenUpdatingNonExistingCandidate() throws Exception {

    UpdateCandidateRequest request = new UpdateCandidateRequest();

    request.setName("Rahul Updated");
    request.setEmail("rahul.updated@example.com");

    when(candidateService.updateCandidate(any(Long.class), any(UpdateCandidateRequest.class)))
        .thenThrow(new ResourceNotFoundException("Candidate not found"));

    String requestJson = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(
            put("/candidates/999").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Candidate not found"));

    verify(candidateService).updateCandidate(any(Long.class), any(UpdateCandidateRequest.class));
  }

  // ---------------------------------------------------------
  // PUT /candidates/{id} - validation failure
  // ---------------------------------------------------------

  @Test
  void shouldReturn400WhenUpdatingInvalidCandidate() throws Exception {

    UpdateCandidateRequest request = new UpdateCandidateRequest();

    request.setName("");
    request.setEmail("invalid-email");

    String requestJson = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(put("/candidates/1").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Validation failed"));

    verify(candidateService, never())
        .updateCandidate(any(Long.class), any(UpdateCandidateRequest.class));
  }

  // ---------------------------------------------------------
  // DELETE /candidates/{id} - success
  // ---------------------------------------------------------

  @Test
  void shouldDeleteCandidate() throws Exception {

    doNothing().when(candidateService).deleteCandidate(1L);

    mockMvc.perform(delete("/candidates/1")).andExpect(status().isOk());

    verify(candidateService).deleteCandidate(1L);
  }

  // ---------------------------------------------------------
  // DELETE /candidates/{id} - not found
  // ---------------------------------------------------------

  @Test
  void shouldReturn404WhenDeletingNonExistingCandidate() throws Exception {

    doThrow(new ResourceNotFoundException("Candidate not found"))
        .when(candidateService)
        .deleteCandidate(999L);

    mockMvc
        .perform(delete("/candidates/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Candidate not found"));

    verify(candidateService).deleteCandidate(999L);
  }
}
