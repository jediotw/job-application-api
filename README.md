# Job Application API

A Spring Boot backend for a job application platform, with a React/Vite frontend.

## Architecture

- **Backend:** Spring Boot, Java 17
- **Database:** PostgreSQL
- **Persistence:** Spring Data JDBC
- **Migrations:** Flyway
- **Authentication:** JWT
- **Frontend:** React + TypeScript + Vite
- **API style:** REST

## Domain Model

The core domain consists of:

- **User** — authentication identity and role.
- **Candidate** — candidate profile and resume URL.
- **Company** — company profile owned logically by a recruiter.
- **Job** — job posting belonging to a company.
- **Application** — candidate's application to a job.

---

## ER Diagram

The ER diagram below reflects the **actual Flyway database schema**. The database currently has five tables: `users`, `candidates`, `companies`, `jobs`, and `applications`.

```mermaid
erDiagram
    USERS {
        BIGINT id PK
        VARCHAR_255 email UK
        VARCHAR_255 password_hash
        VARCHAR_50 role
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    CANDIDATES {
        BIGINT id PK
        VARCHAR_100 name
        VARCHAR_255 email UK
        VARCHAR_20 phone
        VARCHAR_500 resume_url
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    COMPANIES {
        BIGINT id PK
        VARCHAR_300 name UK
        CHAR_21 cin UK
        VARCHAR_500 website
        TEXT description
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    JOBS {
        BIGINT id PK
        BIGINT company_id FK
        VARCHAR_300 title
        TEXT description
        VARCHAR_200 location
        VARCHAR_50 employment_type
        NUMERIC_12_2 salary_min
        NUMERIC_12_2 salary_max
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    APPLICATIONS {
        BIGINT id PK
        BIGINT candidate_id FK
        BIGINT job_id FK
        VARCHAR_50 status
        TIMESTAMP applied_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    COMPANIES ||--o{ JOBS : "has"
    CANDIDATES ||--o{ APPLICATIONS : "submits"
    JOBS ||--o{ APPLICATIONS : "receives"
```

### Database relationship notes

- One **Company** can have many **Jobs**.
- One **Candidate** can submit many **Applications**.
- One **Job** can receive many **Applications**.
- `applications(candidate_id, job_id)` is unique, so a candidate cannot apply to the same job more than once.
- The current database schema does **not** define foreign keys from `candidates` or `companies` to `users`.
- The Java domain models contain `Candidate.userId` and `Company.recruiterId`; these are application-level references rather than database-enforced foreign keys.

---

## UML Class Diagram

This UML view represents the **current Java domain model and application layers**. It intentionally distinguishes application-level references from database foreign keys.

```mermaid
classDiagram

    namespace domain {
        class User {
            -Long id
            -String email
            -String passwordHash
            -String role
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
        }

        class Candidate {
            -Long id
            -Long userId
            -String name
            -String email
            -String phone
            -String resumeUrl
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
        }

        class Company {
            -Long id
            -String name
            -String cin
            -String website
            -String description
            -Long recruiterId
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
        }

        class Job {
            -Long id
            -Long companyId
            -String title
            -String description
            -String location
            -String employmentType
            -BigDecimal salaryMin
            -BigDecimal salaryMax
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
        }

        class Application {
            -Long id
            -Long candidateId
            -Long jobId
            -String status
            -LocalDateTime appliedAt
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
        }
    }

    namespace service {
        class UserService
        class CandidateService
        class CompanyService
        class JobService
        class ApplicationService
    }

    namespace repository {
        class UserRepository
        class CandidateRepository
        class CompanyRepository
        class JobRepository
        class ApplicationRepository
    }

    namespace controller {
        class AuthController
        class CandidateController
        class CompanyController
        class JobController
        class ApplicationController
    }

    Candidate ..> User : userId (logical reference)
    Company ..> User : recruiterId (logical reference)
    Company "1" --> "0..*" Job : companyId
    Candidate "1" --> "0..*" Application : candidateId
    Job "1" --> "0..*" Application : jobId

    AuthController --> UserService
    CandidateController --> CandidateService
    CompanyController --> CompanyService
    JobController --> JobService
    ApplicationController --> ApplicationService

    UserService --> UserRepository
    CandidateService --> CandidateRepository
    CandidateService --> UserService
    CompanyService --> CompanyRepository
    CompanyService --> UserService
    JobService --> JobRepository
    JobService --> CompanyRepository
    ApplicationService --> ApplicationRepository
    ApplicationService --> CandidateRepository
    ApplicationService --> CompanyRepository
```

### Layering

```text
HTTP Request
     |
     v
Controller
     |
     v
Service
     |
     v
Repository
     |
     v
PostgreSQL
```

The services contain the application/business logic, while repositories handle persistence through Spring Data JDBC.

---

## Main API Areas

| Area | Base path | Purpose |
|---|---|---|
| Authentication | `/auth` | Register and login |
| Candidates | `/candidates` | Candidate profile |
| Companies | `/companies` | Company management |
| Jobs | `/jobs` | Job posting management |
| Applications | `/applications` | Applying and application-status management |

## Local Development

### Backend

PostgreSQL is expected on port `5432`.

Set the local environment variables required by the application:

```bash
export JWT_SECRET='your-local-secret-key-at-least-32-characters-long'
export SPRING_DATASOURCE_PASSWORD='your-postgres-password'
```

Then:

```bash
./mvnw spring-boot:run
```

Backend:

```text
http://localhost:8080
```

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

Frontend:

```text
http://localhost:5173
```

During local development, Vite proxies API requests to the Spring Boot backend.

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for the provider-neutral deployment configuration, environment variables, Docker setup, CORS configuration, and frontend/backend deployment requirements.
