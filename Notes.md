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