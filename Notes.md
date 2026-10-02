![img.png](img.png)
# Layer-based / package-by-layer architecture
```
job-application-api/
│
├── pom.xml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── jobapplication/
│   │   │               │
│   │   │               ├── JobApplicationApplication.java
│   │   │               │
│   │   │               ├── controller/
│   │   │               │   ├── CandidateController.java
│   │   │               │   ├── CompanyController.java
│   │   │               │   ├── JobController.java
│   │   │               │   └── ApplicationController.java
│   │   │               │
│   │   │               ├── service/
│   │   │               │   ├── CandidateService.java
│   │   │               │   ├── CompanyService.java
│   │   │               │   ├── JobService.java
│   │   │               │   └── ApplicationService.java
│   │   │               │
│   │   │               ├── repository/
│   │   │               │   ├── CandidateRepository.java
│   │   │               │   ├── CompanyRepository.java
│   │   │               │   ├── JobRepository.java
│   │   │               │   └── ApplicationRepository.java
│   │   │               │
│   │   │               ├── model/
│   │   │               │   ├── Candidate.java
│   │   │               │   ├── Company.java
│   │   │               │   ├── Job.java
│   │   │               │   └── Application.java
│   │   │               │
│   │   │               ├── dto/
│   │   │               │   ├── request/
│   │   │               │   │   ├── CreateCandidateRequest.java
│   │   │               │   │   ├── CreateCompanyRequest.java
│   │   │               │   │   ├── CreateJobRequest.java
│   │   │               │   │   └── CreateApplicationRequest.java
│   │   │               │   │
│   │   │               │   └── response/
│   │   │               │       ├── CandidateResponse.java
│   │   │               │       ├── CompanyResponse.java
│   │   │               │       ├── JobResponse.java
│   │   │               │       └── ApplicationResponse.java
│   │   │               │
│   │   │               ├── enums/
│   │   │               │   ├── ApplicationStatus.java
│   │   │               │   └── JobType.java
│   │   │               │
│   │   │               └── exception/
│   │   │                   ├── ResourceNotFoundException.java
│   │   │                   ├── DuplicateApplicationException.java
│   │   │                   ├── InvalidApplicationStateException.java
│   │   │                   └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── schema.sql
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── jobapplication/
│
├── docker-compose.yml
│
└── README.md
```
# feature oriented packaging
```
job-application-api/
│
├── pom.xml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/jobapplication/
│   │   │       │
│   │   │       ├── JobApplicationApplication.java
│   │   │       │
│   │   │       ├── candidate/
│   │   │       │   ├── controller/
│   │   │       │   ├── service/
│   │   │       │   ├── repository/
│   │   │       │   ├── model/
│   │   │       │   └── dto/
│   │   │       │
│   │   │       ├── company/
│   │   │       │   ├── controller/
│   │   │       │   ├── service/
│   │   │       │   ├── repository/
│   │   │       │   ├── model/
│   │   │       │   └── dto/
│   │   │       │
│   │   │       ├── job/
│   │   │       │   ├── controller/
│   │   │       │   ├── service/
│   │   │       │   ├── repository/
│   │   │       │   ├── model/
│   │   │       │   └── dto/
│   │   │       │
│   │   │       └── application/
│   │   │           ├── controller/
│   │   │           ├── service/
│   │   │           ├── repository/
│   │   │           ├── model/
│   │   │           └── dto/
│   │   │
│   │   └── resources/
│   │       └── application.yml
│   │
│   └── test/
│
└── docker-compose.yml
```