package com.example.jobapplicationapi.company.service;

import com.example.jobapplicationapi.company.dto.CreateCompanyRequest;
import com.example.jobapplicationapi.company.dto.UpdateCompanyRequest;
import com.example.jobapplicationapi.company.model.Company;
import com.example.jobapplicationapi.company.repository.CompanyRepository;
import com.example.jobapplicationapi.exception.AuthorizationException;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import com.example.jobapplicationapi.user.model.User;
import com.example.jobapplicationapi.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

  private final CompanyRepository companyRepository;
  private final UserService userService;

  public CompanyService(CompanyRepository companyRepository, UserService userService) {

    this.companyRepository = companyRepository;
    this.userService = userService;
  }

  public List<Company> getAllCompanies() {

    List<Company> companies = new ArrayList<>();

    Iterable<Company> result = companyRepository.findAll();

    for (Company company : result) {
      companies.add(company);
    }

    return companies;
  }

  public Company getCompanyById(Long id) {

    Optional<Company> result = companyRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Company not found");
    }

    return result.get();
  }

  public Company createCompany(CreateCompanyRequest request) {

    User currentUser = userService.getCurrentUser();

    Company company = new Company();

    company.setRecruiterId(currentUser.getId());
    company.setName(request.getName());
    company.setCin(request.getCin());
    company.setWebsite(request.getWebsite());
    company.setDescription(request.getDescription());

    Company savedCompany = companyRepository.save(company);

    return savedCompany;
  }

  public Company updateCompany(Long id, UpdateCompanyRequest request) {

    Optional<Company> result = companyRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Company not found");
    }

    Company existingCompany = result.get();

    User currentUser = userService.getCurrentUser();

    if (!currentUser.getId().equals(existingCompany.getRecruiterId())) {
      throw new AuthorizationException("You are not allowed to update this company");
    }

    existingCompany.setName(request.getName());
    existingCompany.setCin(request.getCin());
    existingCompany.setWebsite(request.getWebsite());
    existingCompany.setDescription(request.getDescription());

    Company savedCompany = companyRepository.save(existingCompany);

    return savedCompany;
  }

  public void deleteCompany(Long id) {

    Optional<Company> result = companyRepository.findById(id);

    if (!result.isPresent()) {
      throw new ResourceNotFoundException("Company not found");
    }

    Company existingCompany = result.get();

    User currentUser = userService.getCurrentUser();

    if (!currentUser.getId().equals(existingCompany.getRecruiterId())) {
      throw new AuthorizationException("You are not allowed to delete this company");
    }

    companyRepository.deleteById(id);
  }
}
