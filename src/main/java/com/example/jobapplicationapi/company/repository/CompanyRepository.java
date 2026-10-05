package com.example.jobapplicationapi.company.repository;

import com.example.jobapplicationapi.company.model.Company;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository
        extends CrudRepository<Company, Long> {
}
/*
What is happening here?
Same pattern as Candidate:
CompanyRepository
       │
       │ extends
       ▼
CrudRepository<Company, Long>

The two generic types mean:
Company → entity we are storing
Long    → type of its ID

So Spring Data gives us:
save(company)
findAll()
findById(id)
existsById(id)
deleteById(id)
count()

without us writing SQL.
Why don't we add findByName() or findByCin()?
Because we've deliberately chosen to let PostgreSQL enforce:
name VARCHAR(300) UNIQUE
cin  CHAR(21) UNIQUE*/