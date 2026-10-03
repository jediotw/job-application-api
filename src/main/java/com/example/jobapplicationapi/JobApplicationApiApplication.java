package com.example.jobapplicationapi;

import com.example.jobapplicationapi.candidate.repository.CandidateRepository;
import com.example.jobapplicationapi.candidate.service.CandidateService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@SpringBootApplication
@EnableJdbcAuditing
public class JobApplicationApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobApplicationApiApplication.class, args);
    }
   


}
