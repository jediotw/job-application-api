package com.example.jobapplicationapi.application.repository;

import com.example.jobapplicationapi.application.model.Application;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationRepository
        extends CrudRepository<Application, Long> {
}