# CareerOS

CareerOS is an AI-powered job discovery and career management platform. It
continuously monitors target companies, discovers new job postings across
multiple Applicant Tracking Systems, normalizes them into a common domain
model, scores them against a user's preferences, and (eventually) helps
optimize resumes and manage the application pipeline.

This repository currently implements **Milestone 1: the foundation** —
domain model, persistence, and CRUD APIs for companies, job postings, and
user preferences. Nothing beyond that (ATS connectors, scoring, AI resume
optimization, recruiter CRM, notifications) is implemented yet; see
[`docs/architecture.md`](docs/architecture.md) for how the architecture is
designed to accommodate them without rework.

## Tech stack

| Layer | Choice |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 3.3 (Web, Data JPA, Validation, Actuator) |
| Database | PostgreSQL 16 |
| Schema migrations | Flyway (no Hibernate `ddl-auto`) |
| API docs | springdoc-openapi / Swagger UI |
| Build | Maven (wrapper included, no local install required) |
| Containerization | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, AssertJ, Testcontainers |

Frontend (React/TypeScript/Tailwind) and infra-as-code (Terraform/AWS) are
future milestones and not part of this repository yet.

## Architecture at a glance

Modular monolith, Hexagonal Architecture (Ports & Adapters), one module per
business capability (`company`, `job`, `preference`). Full write-up,
Mermaid diagrams, and sequence diagrams for the main request flows are in
[`docs/architecture.md`](docs/architecture.md).

```
com.careeros
├── config/          cross-cutting Spring configuration
├── common/          shared kernel: exceptions, PageResponse, GlobalExceptionHandler
├── company/         domain · application · infrastructure · web
├── job/             domain · application · infrastructure · web
└── preference/      domain · application · infrastructure · web
```

## Getting started

### Prerequisites

- Docker and Docker Compose (recommended path — no local JDK/Maven needed)
- **or**, for local development without Docker: JDK 21 and PostgreSQL 16
  (Maven itself is not required — this repo includes `./mvnw`)

### Option A — run everything with Docker Compose

```bash
docker compose up --build
```

This starts PostgreSQL and the application. Flyway migrations run
automatically on startup. Once healthy:

- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

Copy `.env.example` to `.env` first if you want to override the default DB
credentials or ports.

### Option B — run the app locally against a Dockerized database

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

The app defaults to `localhost:5432` / db `careeros` / user & password
`careeros` (see `src/main/resources/application.yml`); override via the
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` environment
variables.

### Running tests

```bash
./mvnw test      # unit + controller (Mockito/MockMvc) tests — no external services needed
./mvnw verify     # also runs *IT repository tests against a real PostgreSQL via Testcontainers (needs Docker)
```

Fast unit/controller tests (`*Test`) run under `mvn test` via Surefire.
Repository tests that spin up a real PostgreSQL instance (`*IT`, backed by
Testcontainers) run separately under `mvn verify` via Failsafe, so the fast
suite never needs a Docker daemon.

## API overview

All endpoints are under `/api/v1`. Every list endpoint supports pagination
(`page`, `size`) and sorting (`sort=field,asc|desc`); filtering is via query
parameters specific to each resource. Full request/response schemas are in
Swagger UI at `/swagger-ui.html` once the app is running.

| Resource | Endpoints |
|---|---|
| Companies | `POST /companies`, `GET /companies`, `GET /companies/{id}`, `PUT /companies/{id}`, `DELETE /companies/{id}` |
| Jobs | `POST /jobs`, `GET /jobs`, `GET /jobs/{id}`, `PUT /jobs/{id}`, `DELETE /jobs/{id}` |
| Preferences | `POST /preferences`, `GET /preferences`, `GET /preferences/{id}`, `PUT /preferences/{id}`, `DELETE /preferences/{id}` |

Company filters: `name`, `atsType`, `priority`, `enabled`.
Job filters: `companyId`, `title`, `location`, `employmentType`, `remote`, `postedAfter`.

Errors are returned as [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807)
`application/problem+json` bodies (`404` not found, `409` duplicate,
`400` validation, `500` unexpected).

### Example

```bash
curl -X POST http://localhost:8080/api/v1/companies \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Acme Inc",
        "careerUrl": "https://acme.example/careers",
        "atsType": "GREENHOUSE",
        "priority": "HIGH",
        "enabled": true
      }'
```

## Database migrations

Schema is owned entirely by Flyway (`src/main/resources/db/migration`);
Hibernate `ddl-auto` is `none`. Add new migrations as
`V{next}__description.sql` — never edit an already-applied migration.

| Migration | Contents |
|---|---|
| `V1` | `companies` table |
| `V2` | `job_postings` table (FK to companies, unique hash for duplicate detection) |
| `V3` | `user_preferences` + its `roles`/`technologies`/`locations` collection tables |

## Project status / roadmap

- [x] Milestone 1 — foundation: domain model, persistence, CRUD APIs, OpenAPI, tests, Docker
- [ ] ATS connectors (Greenhouse, Lever, Ashby, Workday, SmartRecruiters)
- [ ] Duplicate detection (fuzzy/semantic, beyond the exact-hash check already in place)
- [ ] Job matching / scoring against user preferences
- [ ] AI-assisted resume optimization (OpenAI, pgvector semantic matching)
- [ ] Recruiter CRM
- [ ] Notifications
- [ ] Analytics
- [ ] React/TypeScript/Tailwind frontend
- [ ] Terraform/AWS infrastructure

See [`docs/architecture.md`](docs/architecture.md) for how each of these is
expected to plug into the existing module structure.
