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
##  By extending CrudRepository, Spring Data JDBC provides the standard
// CRUD repository methods for Candidate, using Long as the ID type.
public interface CandidateRepository extends CrudRepository<Candidate, Long> {
}

CandidateRepository
│
│ extends
↓
CrudRepository<Candidate, Long>
│
│ provides contract for
↓
save()
findById()
findAll()
deleteById()
existsById()
count()
│
↓
Spring Data JDBC provides implementation
│
↓
JDBC → PostgreSQL

# Every Java object inherits:
toString()

from java.lang.Object.


Keep the inherited method:
Optional<Candidate> findById(Long id);

Then your service handles the Optional:
public Candidate getCandidateById(Long id) {

    return candidateRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Candidate not found"));
}

Why does Spring use Optional?
Because this:
candidateRepository.findById(999);

might find nothing.
Instead of returning:
null

Spring Data gives you:
Optional<Candidate>

Conceptually:
findById(1)
↓
Optional[Candidate]

or:
findById(999)
↓
Optional.empty()

Then:
.orElseThrow(...)

says:
Give me the Candidate if it exists; otherwise throw this exception.

 **actual return type of each method**.

For `CrudRepository<Candidate, Long>`:

| Method | Return type | Meaning |
|---|---|---|
| `save(candidate)` | `Candidate` | Returns the saved entity |
| `findAll()` | `Iterable<Candidate>` | Returns all candidates |
| `findById(id)` | `Optional<Candidate>` | Candidate if found, otherwise empty |
| `deleteById(id)` | `void` | Deletes the candidate |
| `existsById(id)` | `boolean` | `true` if ID exists |
| `count()` | `long` | Number of candidates |

### 1. `save()`

```java
Candidate savedCandidate = candidateRepository.save(candidate);
```

Returns:

```text
Candidate
```

For a new candidate:

```text
Candidate object
      ↓
   save()
      ↓
   INSERT
      ↓
Candidate with generated ID
```

---

### 2. `findAll()`

```java
Iterable<Candidate> candidates = candidateRepository.findAll();
```

Returns:

```text
Iterable<Candidate>
```

Think:

> "Give me all Candidate objects."

You can loop over it:

```java
for (Candidate candidate : candidates) {
    System.out.println(candidate.getName());
}
```

---

### 3. `findById()`

```java
Optional<Candidate> result = candidateRepository.findById(1L);
```

Returns:

```text
Optional<Candidate>
```

Why `Optional`?

Because candidate `1` may or may not exist.

```text
findById(1)
     ↓
┌─────────────────┐
│ Candidate exists│ → Optional containing Candidate
└─────────────────┘

findById(999)
     ↓
┌──────────────────┐
│ Candidate absent │ → Optional.empty()
└──────────────────┘
```

As a beginner, you can handle it explicitly:

```java
Optional<Candidate> result = candidateRepository.findById(id);

if (result.isPresent()) {
    return result.get();
}

throw new RuntimeException("Candidate not found");
```

---

### 4. `deleteById()`

```java
candidateRepository.deleteById(1L);
```

Returns:

```text
void
```

Meaning:

> It doesn't return a value.

It performs:

```sql
DELETE FROM candidates WHERE id = 1;
```

---

### 5. `existsById()`

```java
boolean exists = candidateRepository.existsById(1L);
```

Returns:

```text
boolean
```

Possible values:

```text
true
false
```

Example:

```java
if (candidateRepository.existsById(id)) {
    System.out.println("Candidate exists");
}
```

---

### 6. `count()`

```java
long total = candidateRepository.count();
```

Returns:

```text
long
```

For example:

```text
5
```

means there are 5 candidates.

---

## Keep this mental cheat sheet

```text
CrudRepository<Candidate, Long>

save()        → Candidate
findAll()     → Iterable<Candidate>
findById()    → Optional<Candidate>
deleteById()  → void
existsById()  → boolean
count()       → long
```

And notice the pattern:

```text
READ multiple  → Iterable
READ one       → Optional
CREATE/UPDATE  → Entity
DELETE         → void
CHECK          → boolean
COUNT          → long
```

One small Java detail: `Long` in `CrudRepository<Candidate, Long>` is the **ID type**, while `long` returned by `count()` is the Java **primitive numeric type**. We'll cover primitives vs wrapper classes as they come up.



Exactly. **There are two common approaches**, and the key difference is **who owns the timestamp generation**.

| Approach | Who generates timestamp? | Typical mechanism |
|---|---|---|
| **Database-generated** | PostgreSQL | `DEFAULT CURRENT_TIMESTAMP` |
| **Spring Data auditing** | Spring Boot / Spring Data | `@CreatedDate`, `@LastModifiedDate` |

### 1. Database owns it

Your current schema:

```sql
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

Flow:

```text
Java
  ↓
Spring Data JDBC
  ↓
PostgreSQL
  ↓
CURRENT_TIMESTAMP
```

This is useful when **the database should be the source of truth**, regardless of which application writes to the database.

---

### 2. Spring Data auditing owns it

Entity:

```java
@CreatedDate
private LocalDateTime createdAt;

@LastModifiedDate
private LocalDateTime updatedAt;
```

And auditing is enabled.

Flow:

```text
Java
  ↓
Spring Data auditing
  ↓
timestamp generated by Spring
  ↓
Spring Data JDBC
  ↓
PostgreSQL
```

This is useful when you want Spring Data to manage entity lifecycle metadata automatically.

---

### The important distinction

Think of it as:

```text
DATABASE AUDITING
        ↓
"DB decides when this row was created/updated"

SPRING DATA AUDITING
        ↓
"Application framework decides when this entity was created/updated"
```

Neither is universally "better." **It's an architectural ownership decision.**

For **our project**, since you explicitly want PostgreSQL to own these timestamps, we'll keep:

```text
PostgreSQL → created_at
PostgreSQL → updated_at
```

and learn Spring Data auditing separately so you understand both patterns.


Why DTOs?
Right now our controller accepts the database entity directly:
@PostMapping
public Candidate createCandidate(@RequestBody Candidate candidate)

That means the client can potentially send fields that shouldn't be controlled by the client:
{
"id": 100,
"createdAt": "2020-01-01T00:00:00",
"updatedAt": "2020-01-01T00:00:00",
"name": "Neha",
"email": "neha@example.com"
}

That's not what we want.
The client should provide candidate input, while the entity represents database state.
So:
HTTP Request
↓
DTO
↓
Service
↓
Entity
↓
Repository
↓
Database


# Java / Spring Boot: JSON, DTO, Entity, and Database Mapping

## Core mental model

```text
JSON
  ↓ Jackson
Request DTO
  ↓ service/application mapping
Entity
  ↓ Spring Data JDBC
PostgreSQL
```

There are **different mappings at different boundaries**.

---

## 1. JSON → DTO

JSON:

```json
{
  "name": "Vikas Kumar",
  "email": "vikas@example.com",
  "resume_url": "https://example.com/vikas-resume"
}
```

DTO:

```java
public class CreateCandidateRequest {

    private String name;
    private String email;
    private String resumeUrl;
}
```

The names differ:

```text
JSON                    Java DTO
--------------------------------
resume_url        →     resumeUrl
```

Jackson converts JSON into the DTO.

### `@JsonProperty`

```java
@JsonProperty("resume_url")
private String resumeUrl;
```

This tells Jackson:

> JSON field `resume_url` maps to Java property `resumeUrl`.

`@JsonProperty` is about **JSON ↔ Java**. It has nothing to do with the database.

---

## 2. DTO → Entity

The DTO and database entity are separate objects.

The service maps between them:

```java
Candidate candidate = new Candidate();

candidate.setName(request.getName());
candidate.setEmail(request.getEmail());
candidate.setPhone(request.getPhone());
candidate.setResumeUrl(request.getResumeUrl());
```

Flow:

```text
CreateCandidateRequest
        ↓
      Service
        ↓
     Candidate
```

This separation prevents clients from directly controlling database fields such as:

```text
id
createdAt
updatedAt
```

---

## 3. Entity → Database

Java entity:

```java
private String resumeUrl;
```

PostgreSQL column:

```text
resume_url
```

Explicit mapping:

```java
@Column("resume_url")
private String resumeUrl;
```

This tells Spring Data JDBC:

> Java property `resumeUrl` is stored in database column `resume_url`.

`@Column` is about **Java Entity ↔ Database**. It has nothing to do with JSON.

---

## 4. The two annotations solve different problems

| Annotation | Boundary | Purpose |
|---|---|---|
| `@JsonProperty("resume_url")` | JSON → Java | Maps JSON field to Java property |
| `@Column("resume_url")` | Java → Database | Maps Java property to DB column |

They may contain the same string, but they operate at different layers.

---

## 5. Complete `resume_url` flow

Client sends:

```json
{
  "resume_url": "https://example.com/resume"
}
```

### JSON → DTO

```text
"resume_url"
     ↓
@JsonProperty("resume_url")
     ↓
CreateCandidateRequest.resumeUrl
```

### DTO → Entity

```java
candidate.setResumeUrl(request.getResumeUrl());
```

### Entity → Database

```text
Candidate.resumeUrl
      ↓
@Column("resume_url")
      ↓
candidates.resume_url
```

Complete flow:

```text
JSON
"resume_url"
      │
      │ @JsonProperty
      ▼
DTO
resumeUrl
      │
      │ service mapping
      ▼
Entity
resumeUrl
      │
      │ @Column
      ▼
PostgreSQL
resume_url
```

---

## 6. Go mental model

In Go, a JSON tag solves the JSON mapping problem:

```go
type CreateCandidateRequest struct {
    Name      string `json:"name"`
    Email     string `json:"email"`
    ResumeURL string `json:"resume_url"`
}
```

This is conceptually similar to:

```java
@JsonProperty("resume_url")
private String resumeUrl;
```

The database mapping is a separate concern.

So think:

```text
JSON mapping
    ↓
request DTO
    ↓
application mapping
    ↓
entity
    ↓
database mapping
    ↓
SQL table
```

---

## 7. Why use `@Column`?

Our database uses snake_case:

```text
resume_url
created_at
updated_at
```

Java uses camelCase:

```text
resumeUrl
createdAt
updatedAt
```

Explicit mapping makes the database relationship obvious:

```java
@Column("resume_url")
private String resumeUrl;

@Column("created_at")
private LocalDateTime createdAt;

@Column("updated_at")
private LocalDateTime updatedAt;
```

---

## 8. Why use `@JsonProperty`?

Our API request uses:

```json
"resume_url"
```

while Java uses:

```java
resumeUrl
```

So we explicitly tell Jackson:

```java
@JsonProperty("resume_url")
private String resumeUrl;
```

A global Jackson `snake_case` naming strategy is another option, but explicit annotations are useful when you want a mapping to be obvious or override the default convention.

---

## 9. Final mental model

Always ask:

> **Which two worlds am I mapping between?**

### JSON ↔ Java DTO

```java
@JsonProperty
```

### DTO ↔ Entity

Application/service code:

```java
candidate.setResumeUrl(request.getResumeUrl());
```

### Java Entity ↔ Database

```java
@Column
```

A single field can therefore have multiple representations:

```text
HTTP JSON
    │
    │ "resume_url"
    ▼
Request DTO
    │
    │ resumeUrl
    ▼
Entity
    │
    │ resume_url
    ▼
PostgreSQL
```

## One-line rule

> **`@JsonProperty` is for JSON mapping. `@Column` is for database mapping. DTO → Entity mapping is application/service code.**