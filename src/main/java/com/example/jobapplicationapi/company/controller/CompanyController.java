package com.example.jobapplicationapi.company.controller;

import com.example.jobapplicationapi.company.dto.CreateCompanyRequest;
import com.example.jobapplicationapi.company.dto.UpdateCompanyRequest;
import com.example.jobapplicationapi.company.model.Company;
import com.example.jobapplicationapi.company.service.CompanyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies")
public class CompanyController {

  private final CompanyService companyService;

  public CompanyController(CompanyService companyService) {
    this.companyService = companyService;
  }

  @GetMapping
  public List<Company> getAllCompanies() {
    return companyService.getAllCompanies();
  }

  @GetMapping("/{id}")
  public Company getCompanyById(@PathVariable Long id) {
    return companyService.getCompanyById(id);
  }

  @PostMapping
  public Company createCompany(@Valid @RequestBody CreateCompanyRequest request) {

    Company savedCompany = companyService.createCompany(request);

    return savedCompany;
  }

  @PutMapping("/{id}")
  public Company updateCompany(
      @PathVariable Long id, @Valid @RequestBody UpdateCompanyRequest request) {

    Company updatedCompany = companyService.updateCompany(id, request);

    return updatedCompany;
  }

  @DeleteMapping("/{id}")
  public void deleteCompany(@PathVariable Long id) {
    companyService.deleteCompany(id);
  }
}
