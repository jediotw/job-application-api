package com.example.jobapplicationapi.company.service;

import com.example.jobapplicationapi.company.dto.CreateCompanyRequest;
import com.example.jobapplicationapi.company.dto.UpdateCompanyRequest;
import com.example.jobapplicationapi.company.model.Company;
import com.example.jobapplicationapi.company.repository.CompanyRepository;
import com.example.jobapplicationapi.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
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

        Optional<Company> result =
                companyRepository.findById(id);

        if (!result.isPresent()) {
            throw new ResourceNotFoundException("Company not found");
        }

        return result.get();
    }

    public Company createCompany(CreateCompanyRequest request) {

        Company company = new Company();

        company.setName(request.getName());
        company.setCin(request.getCin());
        company.setWebsite(request.getWebsite());
        company.setDescription(request.getDescription());

        Company savedCompany =
                companyRepository.save(company);

        return savedCompany;
    }

    public Company updateCompany(
            Long id,
            UpdateCompanyRequest request) {

        Optional<Company> result =
                companyRepository.findById(id);

        if (!result.isPresent()) {
            throw new ResourceNotFoundException("Company not found");
        }

        Company existingCompany = result.get();

        existingCompany.setName(request.getName());
        existingCompany.setCin(request.getCin());
        existingCompany.setWebsite(request.getWebsite());
        existingCompany.setDescription(request.getDescription());

        Company savedCompany =
                companyRepository.save(existingCompany);

        return savedCompany;
    }

    public void deleteCompany(Long id) {

        boolean exists =
                companyRepository.existsById(id);

        if (!exists) {
            throw new ResourceNotFoundException("Company not found");
        }

        companyRepository.deleteById(id);
    }
}