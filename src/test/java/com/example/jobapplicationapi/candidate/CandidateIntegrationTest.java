package com.example.jobapplicationapi.candidate;

import com.example.jobapplicationapi.application.repository.ApplicationRepository;
import com.example.jobapplicationapi.candidate.model.Candidate;
import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class CandidateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    // ---------------------------------------------------------
    // Clean database before every test
    // ---------------------------------------------------------

    @BeforeEach
    void cleanDatabase() {
        applicationRepository.deleteAll();
        candidateRepository.deleteAll();
    }


    // ---------------------------------------------------------
    // GET /candidates
    // ---------------------------------------------------------

    @Test
    void shouldGetAllCandidates() throws Exception {

        Candidate candidate1 = new Candidate();

        candidate1.setName("Rahul Sharma");
        candidate1.setEmail("rahul@example.com");
        candidate1.setPhone("9876543210");
        candidate1.setResumeUrl(
                "https://example.com/rahul.pdf"
        );

        candidateRepository.save(candidate1);


        Candidate candidate2 = new Candidate();

        candidate2.setName("Amit Kumar");
        candidate2.setEmail("amit@example.com");
        candidate2.setPhone("9999999999");
        candidate2.setResumeUrl(
                "https://example.com/amit.pdf"
        );

        candidateRepository.save(candidate2);


        mockMvc.perform(
                        get("/candidates")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Rahul Sharma")
                )
                .andExpect(
                        jsonPath("$[0].email")
                                .value("rahul@example.com")
                )
                .andExpect(
                        jsonPath("$[1].name")
                                .value("Amit Kumar")
                )
                .andExpect(
                        jsonPath("$[1].email")
                                .value("amit@example.com")
                );
    }


    // ---------------------------------------------------------
    // GET /candidates/{id} - success
    // ---------------------------------------------------------

    @Test
    void shouldGetCandidateById() throws Exception {

        Candidate candidate = new Candidate();

        candidate.setName("Rahul Sharma");
        candidate.setEmail("rahul@example.com");
        candidate.setPhone("9876543210");
        candidate.setResumeUrl(
                "https://example.com/rahul.pdf"
        );


        Candidate savedCandidate =
                candidateRepository.save(candidate);


        mockMvc.perform(
                        get(
                                "/candidates/"
                                        + savedCandidate.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        savedCandidate.getId()
                                )
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Rahul Sharma")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("rahul@example.com")
                )
                .andExpect(
                        jsonPath("$.phone")
                                .value("9876543210")
                )
                .andExpect(
                        jsonPath("$.resumeUrl")
                                .value(
                                        "https://example.com/rahul.pdf"
                                )
                );
    }


    // ---------------------------------------------------------
    // GET /candidates/{id} - not found
    // ---------------------------------------------------------

    @Test
    void shouldReturn404WhenCandidateDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get("/candidates/999999")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Candidate not found")
                );
    }


    // ---------------------------------------------------------
    // POST /candidates - success
    // ---------------------------------------------------------

    @Test
    void shouldCreateCandidate() throws Exception {

        String requestJson = """
                {
                    "name": "Rahul Sharma",
                    "email": "rahul@example.com",
                    "phone": "9876543210",
                    "resume_url": "https://example.com/rahul.pdf"
                }
                """;


        mockMvc.perform(
                        post("/candidates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Rahul Sharma")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("rahul@example.com")
                )
                .andExpect(
                        jsonPath("$.phone")
                                .value("9876543210")
                )
                .andExpect(
                        jsonPath("$.resumeUrl")
                                .value(
                                        "https://example.com/rahul.pdf"
                                )
                )
                .andExpect(
                        jsonPath("$.createdAt")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.updatedAt")
                                .exists()
                );
    }


    // ---------------------------------------------------------
    // POST /candidates - validation failure
    // ---------------------------------------------------------

    @Test
    void shouldReturn400WhenCreatingInvalidCandidate()
            throws Exception {

        String requestJson = """
                {
                    "name": "",
                    "email": "invalid-email",
                    "phone": "9876543210"
                }
                """;


        mockMvc.perform(
                        post("/candidates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )
                .andExpect(
                        jsonPath("$.errors.name")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.errors.email")
                                .exists()
                );
    }


    // ---------------------------------------------------------
    // POST /candidates - duplicate email
    // ---------------------------------------------------------

    @Test
    void shouldReturn409WhenEmailAlreadyExists()
            throws Exception {

        Candidate candidate = new Candidate();

        candidate.setName("Existing Candidate");
        candidate.setEmail("duplicate@example.com");
        candidate.setPhone("1111111111");

        candidateRepository.save(candidate);


        String requestJson = """
                {
                    "name": "Another Candidate",
                    "email": "duplicate@example.com",
                    "phone": "2222222222"
                }
                """;


        mockMvc.perform(
                        post("/candidates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Data conflict")
                );
    }


    // ---------------------------------------------------------
    // PUT /candidates/{id} - success
    // ---------------------------------------------------------

    @Test
    void shouldUpdateCandidate() throws Exception {

        Candidate candidate = new Candidate();

        candidate.setName("Rahul Sharma");
        candidate.setEmail("rahul@example.com");
        candidate.setPhone("9876543210");
        candidate.setResumeUrl(
                "https://example.com/rahul.pdf"
        );


        Candidate savedCandidate =
                candidateRepository.save(candidate);


        String requestJson = """
                {
                    "name": "Rahul Updated",
                    "email": "rahul.updated@example.com",
                    "phone": "9999999999",
                    "resume_url": "https://example.com/updated.pdf"
                }
                """;


        mockMvc.perform(
                        put(
                                "/candidates/"
                                        + savedCandidate.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        savedCandidate.getId()
                                )
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Rahul Updated")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "rahul.updated@example.com"
                                )
                )
                .andExpect(
                        jsonPath("$.phone")
                                .value("9999999999")
                )
                .andExpect(
                        jsonPath("$.resumeUrl")
                                .value(
                                        "https://example.com/updated.pdf"
                                )
                );
    }


    // ---------------------------------------------------------
    // PUT /candidates/{id} - not found
    // ---------------------------------------------------------

    @Test
    void shouldReturn404WhenUpdatingNonExistingCandidate()
            throws Exception {

        String requestJson = """
                {
                    "name": "Rahul Updated",
                    "email": "rahul.updated@example.com",
                    "phone": "9999999999"
                }
                """;


        mockMvc.perform(
                        put("/candidates/999999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Candidate not found")
                );
    }


    // ---------------------------------------------------------
    // PUT /candidates/{id} - validation failure
    // ---------------------------------------------------------

    @Test
    void shouldReturn400WhenUpdatingInvalidCandidate()
            throws Exception {

        String requestJson = """
                {
                    "name": "",
                    "email": "invalid-email",
                    "phone": "9999999999"
                }
                """;


        mockMvc.perform(
                        put("/candidates/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )
                .andExpect(
                        jsonPath("$.errors.name")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.errors.email")
                                .exists()
                );
    }


    // ---------------------------------------------------------
    // PUT /candidates/{id} - duplicate email
    // ---------------------------------------------------------

    @Test
    void shouldReturn409WhenUpdatingWithExistingEmail()
            throws Exception {

        Candidate candidate1 = new Candidate();

        candidate1.setName("Rahul Sharma");
        candidate1.setEmail("rahul@example.com");
        candidate1.setPhone("9876543210");

        candidateRepository.save(candidate1);


        Candidate candidate2 = new Candidate();

        candidate2.setName("Amit Kumar");
        candidate2.setEmail("amit@example.com");
        candidate2.setPhone("9999999999");

        Candidate savedCandidate =
                candidateRepository.save(candidate2);


        String requestJson = """
                {
                    "name": "Amit Updated",
                    "email": "rahul@example.com",
                    "phone": "8888888888"
                }
                """;


        mockMvc.perform(
                        put(
                                "/candidates/"
                                        + savedCandidate.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Data conflict")
                );
    }


    // ---------------------------------------------------------
    // DELETE /candidates/{id} - success
    // ---------------------------------------------------------

    @Test
    void shouldDeleteCandidate() throws Exception {

        Candidate candidate = new Candidate();

        candidate.setName("Rahul Sharma");
        candidate.setEmail("rahul@example.com");
        candidate.setPhone("9876543210");


        Candidate savedCandidate =
                candidateRepository.save(candidate);


        mockMvc.perform(
                        delete(
                                "/candidates/"
                                        + savedCandidate.getId()
                        )
                )
                .andExpect(status().isOk());


        boolean exists =
                candidateRepository.existsById(
                        savedCandidate.getId()
                );


        if (exists) {
            throw new AssertionError(
                    "Candidate was not deleted"
            );
        }
    }


    // ---------------------------------------------------------
    // DELETE /candidates/{id} - not found
    // ---------------------------------------------------------

    @Test
    void shouldReturn404WhenDeletingNonExistingCandidate()
            throws Exception {

        mockMvc.perform(
                        delete("/candidates/999999")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Candidate not found")
                );
    }
}