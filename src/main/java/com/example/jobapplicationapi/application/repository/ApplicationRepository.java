package com.example.jobapplicationapi.application.repository;

import com.example.jobapplicationapi.application.model.Application;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationRepository extends CrudRepository<Application, Long> {

  List<Application> findByCandidateId(Long candidateId);

  @Query(
      """
      SELECT a.*
      FROM applications a
      JOIN jobs j ON a.job_id = j.id
      JOIN companies c ON j.company_id = c.id
      WHERE c.recruiter_id = :recruiterId
      """)
  List<Application> findByRecruiterId(Long recruiterId);
}
