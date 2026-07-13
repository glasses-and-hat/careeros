# CareerOS — Architecture (Milestone 1)

CareerOS is built as a **modular monolith** using **Hexagonal Architecture
(Ports & Adapters)**. Each business capability (company, job, preference) is
a self-contained module with its own domain, application, infrastructure,
and web layers. Modules communicate through public domain types, never
through infrastructure details.

## Why hexagonal, and why a monolith first

The eventual system needs ATS connectors (Greenhouse, Lever, Ashby, Workday,
SmartRecruiters), a scoring engine, a resume-optimization AI pipeline, and a
recruiter CRM. Those are naturally pluggable adapters, not new services — a
Greenhouse connector is an inbound adapter that produces `JobPosting`
aggregates the same way the REST API's create endpoint does. Splitting into
microservices before the domain model has stabilized would mean distributed
transactions across boundaries that aren't proven yet. A modular monolith
with strict ports keeps deployment simple while making the eventual
extraction of a service (if ever needed) a matter of swapping an adapter,
not rewriting the domain.

## Layer diagram

```mermaid
graph TB
    subgraph "Web Layer (inbound adapters)"
        CC[CompanyController]
        JC[JobPostingController]
        PC[UserPreferenceController]
        GEH[GlobalExceptionHandler]
    end

    subgraph "Application Layer (use cases)"
        CS[CompanyService]
        JS[JobPostingService]
        PS[UserPreferenceService]
    end

    subgraph "Domain Layer (business model, framework-free)"
        CE[Company]
        JE[JobPosting]
        PE[UserPreference]
        CRP["CompanyRepository (port)"]
        JRP["JobPostingRepository (port)"]
        PRP["UserPreferenceRepository (port)"]
    end

    subgraph "Infrastructure Layer (outbound adapters)"
        CRA[CompanyRepositoryAdapter]
        JRA[JobPostingRepositoryAdapter]
        PRA[UserPreferenceRepositoryAdapter]
        SDR[(Spring Data JPA)]
        PG[(PostgreSQL)]
    end

    CC --> CS
    JC --> JS
    PC --> PS

    CS --> CRP
    JS --> JRP
    JS -.reads.-> CRP
    PS --> PRP

    CS -.operates on.-> CE
    JS -.operates on.-> JE
    PS -.operates on.-> PE

    CRA -.implements.-> CRP
    JRA -.implements.-> JRP
    PRA -.implements.-> PRP

    CRA --> SDR
    JRA --> SDR
    PRA --> SDR
    SDR --> PG
```

**Dependency rule**: arrows only point inward. The domain layer imports
nothing from `application`, `web`, or `infrastructure`. The application
layer depends on domain *ports* (interfaces), never on
`infrastructure.persistence` classes — those are package-private and
invisible outside their module.

## Module layout

```
com.careeros
├── CareerOsApplication.java
├── config/                     # cross-cutting Spring configuration (OpenAPI, JPA auditing)
├── common/
│   ├── domain/                 # AuditableEntity (shared base, not a "god class")
│   ├── exception/               # ResourceNotFoundException, DuplicateResourceException, ...
│   └── web/                    # GlobalExceptionHandler, PageResponse<T>
├── company/
│   ├── domain/                 # Company, AtsType, Priority, CompanyRepository (port), CompanyFilter
│   ├── application/            # CompanyService, CompanyCommand
│   ├── infrastructure/persistence/  # Spring Data JPA repository + Specifications + adapter
│   └── web/                    # CompanyController, request/response DTOs, mapper
├── job/
│   ├── domain/                 # JobPosting, EmploymentType, SalaryRange, JobPostingRepository (port)
│   ├── application/            # JobPostingService, JobPostingCommand
│   ├── infrastructure/persistence/
│   └── web/
└── preference/
    ├── domain/                 # UserPreference, UserPreferenceRepository (port)
    ├── application/            # UserPreferenceService, UserPreferenceCommand
    ├── infrastructure/persistence/
    └── web/
```

Within each module, only `domain` types are public across module
boundaries. `infrastructure.persistence` classes (the Spring Data
repository interface and its adapter) are package-private — nothing outside
the module can accidentally depend on JPA specifics. `job` depends on
`company.domain` (a `JobPosting` belongs to a `Company`), which is the one
intentional cross-module dependency in milestone 1; `company` and
`preference` have no dependencies on sibling modules.

## Request flow: creating a job posting

```mermaid
sequenceDiagram
    actor Client
    participant Controller as JobPostingController
    participant Service as JobPostingService
    participant CompanyPort as CompanyRepository (port)
    participant JobPort as JobPostingRepository (port)
    participant DB as PostgreSQL

    Client->>Controller: POST /api/v1/jobs (JobPostingRequest)
    Controller->>Controller: validate request (Bean Validation)
    Controller->>Service: create(JobPostingCommand)
    Service->>CompanyPort: findById(companyId)
    CompanyPort->>DB: SELECT ... FROM companies WHERE id = ?
    DB-->>CompanyPort: Company row
    CompanyPort-->>Service: Company

    alt company not found
        Service-->>Controller: throws ResourceNotFoundException
        Controller-->>Client: 404 Problem Detail
    end

    Service->>Service: JobPosting.create(...) — computes SHA-256 hash(companyId, externalId)
    Service->>JobPort: existsByHash(hash)
    JobPort->>DB: SELECT ... FROM job_postings WHERE hash = ?

    alt duplicate hash
        Service-->>Controller: throws DuplicateResourceException
        Controller-->>Client: 409 Problem Detail
    end

    Service->>JobPort: save(jobPosting)
    JobPort->>DB: INSERT INTO job_postings (...)
    DB-->>JobPort: persisted row (id, timestamps)
    JobPort-->>Service: JobPosting
    Service-->>Controller: JobPosting
    Controller-->>Client: 201 Created + JobPostingResponse
```

The hash-based duplicate check is deliberately narrow for milestone 1: it
only catches an exact re-post of the same `(company, externalId)` pair. The
future **duplicate detection** feature (fuzzy/semantic matching across
re-postings, relisted roles, and near-duplicate titles) builds on top of
this — the hash stays as a cheap first-pass filter before anything more
expensive runs.

## Request flow: filtered, paginated listing

```mermaid
sequenceDiagram
    actor Client
    participant Controller as JobPostingController
    participant Service as JobPostingService
    participant Port as JobPostingRepository (port)
    participant Adapter as JobPostingRepositoryAdapter
    participant Spec as JobPostingSpecifications
    participant DB as PostgreSQL

    Client->>Controller: GET /api/v1/jobs?remote=true&page=0&size=20&sort=postedDate,desc
    Controller->>Controller: bind JobPostingFilter + Pageable
    Controller->>Service: list(filter, pageable)
    Service->>Port: findAll(filter, pageable)
    Port->>Adapter: findAll(filter, pageable)
    Adapter->>Spec: fromFilter(filter)
    Spec-->>Adapter: Specification (predicates + company fetch join)
    Adapter->>DB: SELECT ... WHERE remote = true ORDER BY posted_date DESC LIMIT 20
    DB-->>Adapter: Page<JobPosting>
    Adapter-->>Service: Page<JobPosting>
    Service-->>Controller: Page<JobPosting>
    Controller->>Controller: map to PageResponse<JobPostingResponse>
    Controller-->>Client: 200 OK
```

## Cross-cutting concerns

- **Validation**: Jakarta Bean Validation on request DTOs (`@NotBlank`,
  `@Size`, `@Min`/`@Max`); domain invariants (e.g. `minimumScore` in
  [0, 100]) are additionally enforced inside the entity so they hold even
  for callers that bypass the web layer (future batch jobs, connectors).
- **Error handling**: every error response is an
  [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) `ProblemDetail` —
  `404` for missing resources, `409` for uniqueness violations, `400` for
  validation failures (with a field-level `errors` map), `500` for
  anything unexpected (logged, never leaked to the client).
- **Persistence**: no Hibernate `ddl-auto` — schema is owned entirely by
  Flyway migrations in `src/main/resources/db/migration`. IDs are
  DB-generated UUIDs (via Hibernate's `@UuidGenerator`), which avoids
  exposing sequential identifiers and works cleanly once the system needs
  to merge data from multiple ingestion sources.
- **Auditing**: `createdAt`/`updatedAt` are managed by Spring Data JPA
  auditing (`AuditableEntity`), never set manually — one less thing for
  every service method to get wrong.

## Known Hibernate 6 pitfalls this codebase already worked around

Worth documenting since they're easy to reintroduce:

1. **Lazy associations mapped to a DTO outside the transaction.**
   `open-in-view` is disabled (correct for production — it hides N+1s and
   holds connections open needlessly). That means any lazy association
   (`JobPosting.company`) accessed after the `@Transactional` service
   method returns throws `LazyInitializationException`. Fixed via
   `@EntityGraph` on `findById` and an explicit fetch-join `Specification`
   for list queries — not by flipping the association to `EAGER` globally
   or re-enabling `open-in-view`.
2. **An all-null `@Embeddable` deserializes as `null`, not an all-null
   instance.** When every column backing `SalaryRange` is `NULL`,
   Hibernate returns `null` for the field instead of `SalaryRange(null,
   null, null)`. `JobPosting.getSalary()` normalizes this at the domain
   boundary so no caller has to null-check it.
3. **`@Embeddable` Java records can silently misorder constructor
   arguments.** Hibernate 6 resolves embeddable attributes alphabetically
   by name but calls a record's canonical constructor positionally; if the
   record's declared field order isn't already alphabetical, values get
   passed into the wrong parameters (observed as a `ClassCastException`
   deep in `HibernateJpaDialect`, e.g. a `String` currency landing in a
   `BigDecimal` slot). `SalaryRange` is a plain class with an explicit
   constructor to sidestep that ordering assumption rather than rely on it.

## Extension points for future milestones

| Future capability | Where it plugs in |
|---|---|
| Greenhouse / Lever / Ashby / Workday / SmartRecruiters connectors | New inbound adapters that call `JobPostingService.create(...)` / `CompanyService`, one per ATS, likely under `job/infrastructure/ingestion/<ats>/`. `Company.atsType` already selects which connector owns a company. |
| Duplicate detection (fuzzy) | A new domain service consuming `JobPostingRepository`, layered on top of the existing hash check. |
| Job matching / scoring | A new `matching` module reading `UserPreference` + `JobPosting` through their existing ports; no changes needed to either aggregate. |
| AI resume optimization | A new module behind a `ResumeOptimizer` port, with an OpenAI-backed adapter — same pattern as persistence ports. |
| Recruiter CRM | A new bounded context (`recruiter/`) following the same domain/application/infrastructure/web shape. |
| Notifications, analytics | New outbound ports invoked from the application layer of the modules that produce the events (e.g. `JobPostingService.create` publishing a domain event once an event bus exists). |

None of this is implemented yet — milestone 1 is deliberately just the
foundation (companies, jobs, preferences, CRUD, persistence, docs). The
point of the layering above is that none of it requires touching the
`domain` or `application` code that already exists.
