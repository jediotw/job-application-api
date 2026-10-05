package com.example.jobapplicationapi.job.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("jobs")
public class Job {

  @Id private Long id;

  @Column("company_id")
  private Long companyId;

  private String title;

  private String description;

  private String location;

  @Column("employment_type")
  private String employmentType;

  @Column("salary_min")
  private BigDecimal salaryMin;

  @Column("salary_max")
  private BigDecimal salaryMax;

  @Column("created_at")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column("updated_at")
  @LastModifiedDate
  private LocalDateTime updatedAt;

  public Job() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getEmploymentType() {
    return employmentType;
  }

  public void setEmploymentType(String employmentType) {
    this.employmentType = employmentType;
  }

  public BigDecimal getSalaryMin() {
    return salaryMin;
  }

  public void setSalaryMin(BigDecimal salaryMin) {
    this.salaryMin = salaryMin;
  }

  public BigDecimal getSalaryMax() {
    return salaryMax;
  }

  public void setSalaryMax(BigDecimal salaryMax) {
    this.salaryMax = salaryMax;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
